package dev.emreozcan.pazarsync.orderservice;

import dev.emreozcan.pazarsync.events.EventCodec;
import dev.emreozcan.pazarsync.events.EventKeys;
import dev.emreozcan.pazarsync.events.KafkaTopics;
import dev.emreozcan.pazarsync.events.OrderLinePayload;
import dev.emreozcan.pazarsync.events.OrderReceived;
import dev.emreozcan.pazarsync.orderservice.domain.CustomerOrderRepository;
import dev.emreozcan.pazarsync.orderservice.domain.Product;
import dev.emreozcan.pazarsync.orderservice.domain.ProductRepository;
import dev.emreozcan.pazarsync.orderservice.domain.StoredSyncFailureRepository;
import dev.emreozcan.pazarsync.orderservice.ingest.IngestResult;
import dev.emreozcan.pazarsync.orderservice.ingest.InsufficientStockException;
import dev.emreozcan.pazarsync.orderservice.ingest.OrderIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OrderServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles({"test", "ai-stub"})
@ContextConfiguration(initializers = OrderServiceIntegrationTest.ContainerInitializer.class)
@Testcontainers(disabledWithoutDocker = true)
class OrderServiceIntegrationTest {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("pgvector/pgvector:pg16");

    @Container
    static final KafkaContainer kafka = new KafkaContainer("apache/kafka:3.8.1");

    static class ContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword(),
                    "spring.kafka.bootstrap-servers=" + kafka.getBootstrapServers()
            ).applyTo(context.getEnvironment());
        }
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private CustomerOrderRepository orders;

    @Autowired
    private ProductRepository products;

    @Autowired
    private StoredSyncFailureRepository failures;

    @Autowired
    private OrderIngestionService ingestion;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void duplicateEventCreatesOneOrderAndOneStockDecrement() throws Exception {
        OrderReceived event = new OrderReceived(
                "trendyol",
                "TY-DUP",
                "Ada Lovelace",
                Instant.parse("2026-06-01T12:00:00Z"),
                List.of(new OrderLinePayload("BARCODE-MUG", "Stoneware mug", 1, 24900)));
        String json = EventCodec.write(event);
        String key = EventKeys.of(event.marketplace(), event.externalOrderId());
        kafkaTemplate.send(KafkaTopics.ORDERS_RECEIVED, key, json).get(10, TimeUnit.SECONDS);
        kafkaTemplate.send(KafkaTopics.ORDERS_RECEIVED, key, json).get(10, TimeUnit.SECONDS);

        await().atMost(Duration.ofSeconds(30)).until(() ->
                orders.countByMarketplaceAndExternalOrderId("trendyol", "TY-DUP") == 1
                        && quantity("BARCODE-MUG") == 39);

        Integer movements = jdbc.queryForObject(
                "select count(*) from stock_movements where product_id = ?",
                Integer.class,
                products.findByBarcode("BARCODE-MUG").orElseThrow().getId());
        assertThat(movements).isEqualTo(1);
    }

    @Test
    void parallelOrdersDoNotOversell() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        List<Future<IngestResult>> futures = new ArrayList<>();
        try {
            for (int index = 0; index < 10; index++) {
                int n = index;
                futures.add(pool.submit(() -> ingestion.ingest(new OrderReceived(
                        "trendyol",
                        "BAG-" + n,
                        "Can Demir",
                        Instant.parse("2026-06-02T12:00:00Z"),
                        List.of(new OrderLinePayload("BARCODE-BAG", "Canvas tote", 1, 54900))))));
            }
            int created = 0;
            int rejected = 0;
            for (Future<IngestResult> future : futures) {
                try {
                    if (future.get(15, TimeUnit.SECONDS) == IngestResult.CREATED) {
                        created++;
                    }
                } catch (ExecutionException ex) {
                    if (ex.getCause() instanceof InsufficientStockException) {
                        rejected++;
                    } else {
                        throw ex;
                    }
                }
            }
            assertThat(created).isEqualTo(5);
            assertThat(rejected).isEqualTo(5);
            assertThat(quantity("BARCODE-BAG")).isEqualTo(0);
            Integer bagOrders = jdbc.queryForObject(
                    "select count(*) from orders where external_order_id like 'BAG-%'",
                    Integer.class);
            assertThat(bagOrders).isEqualTo(5);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void poisonMessageIsStoredAsAFailedSync() throws Exception {
        kafkaTemplate.send(KafkaTopics.ORDERS_RECEIVED, "bad", "NOT_JSON").get(10, TimeUnit.SECONDS);

        await().atMost(Duration.ofSeconds(20)).until(() -> failures.count() >= 1);
        assertThat(failures.findAll()).anyMatch(failure -> "POISON".equals(failure.getOperation()));
    }

    @Test
    void endpointsRejectMissingAndWrongRoles() throws Exception {
        mockMvc.perform(get("/api/orders")).andExpect(status().isUnauthorized());

        String viewer = token("viewer", "viewer");
        mockMvc.perform(post("/api/questions/" + UUID.randomUUID() + "/approve")
                        .header("Authorization", "Bearer " + viewer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"no\"}"))
                .andExpect(status().isForbidden());

        String admin = token("admin", "admin");
        mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
    }

    @Test
    void vectorSearchReturnsTheNearestPastAnswer() {
        vectorStore.add(List.of(
                new Document("The mug ships in two days", Map.of("id", "mug")),
                new Document("Invoice tax identifier", Map.of("id", "tax"))));

        List<Document> hits = vectorStore.similaritySearch(SearchRequest.builder()
                .query("when does the mug ship")
                .topK(1)
                .build());

        assertThat(hits).isNotEmpty();
        assertThat(hits.getFirst().getText()).contains("mug");
    }

    private int quantity(String barcode) {
        return products.findByBarcode(barcode).map(Product::getQuantity).orElseThrow();
    }

    private String token(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();
        int start = body.indexOf("\"token\":\"") + "\"token\":\"".length();
        return body.substring(start, body.indexOf('"', start));
    }
}
