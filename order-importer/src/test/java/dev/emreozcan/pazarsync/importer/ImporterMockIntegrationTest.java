package dev.emreozcan.pazarsync.importer;

import dev.emreozcan.pazarsync.events.KafkaTopics;
import dev.emreozcan.pazarsync.mock.MarketplaceLine;
import dev.emreozcan.pazarsync.mock.MarketplaceOrder;
import dev.emreozcan.pazarsync.mock.MarketplaceStore;
import dev.emreozcan.pazarsync.mock.MockMarketplaceApplication;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OrderImporterApplication.class)
@ContextConfiguration(initializers = ImporterMockIntegrationTest.ContainerInitializer.class)
@Testcontainers(disabledWithoutDocker = true)
class ImporterMockIntegrationTest {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Container
    static final KafkaContainer kafka = new KafkaContainer("apache/kafka:3.8.1");

    private static ConfigurableApplicationContext mock;

    static class ContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            if (mock == null) {
                mock = new SpringApplicationBuilder(MockMarketplaceApplication.class)
                        .run(
                                "--server.port=0",
                                "--spring.main.banner-mode=off",
                                "--pazarsync.mock.generator.enabled=false",
                                "--pazarsync.mock.faults.enabled=true",
                                "--pazarsync.mock.faults.failure-rate=0",
                                "--pazarsync.mock.faults.duplicate-rate=0",
                                "--pazarsync.mock.faults.fail-first=2",
                                "--management.endpoint.health.group.readiness.include=livenessState",
                                "--spring.autoconfigure.exclude="
                                        + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                                        + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                                        + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration,"
                                        + "org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration");
            }
            String port = mock.getEnvironment().getProperty("local.server.port");
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword(),
                    "spring.kafka.bootstrap-servers=" + kafka.getBootstrapServers(),
                    "pazarsync.importer.marketplace-base-url=http://127.0.0.1:" + port,
                    "pazarsync.importer.poll-delay-ms=600000",
                    "pazarsync.importer.retry.initial-backoff-ms=20",
                    "pazarsync.importer.retry.max-attempts=5"
            ).applyTo(context.getEnvironment());
        }
    }

    @AfterAll
    static void stopMock() {
        if (mock != null) {
            mock.close();
            mock = null;
        }
    }

    @Autowired
    private MarketplacePoller poller;

    @Test
    void retriesInjectedFaultsAndPublishesTheOrder() {
        try (KafkaConsumer<String, String> consumer = consumer()) {
            consumer.subscribe(List.of(KafkaTopics.ORDERS_RECEIVED));
            consumer.poll(Duration.ofSeconds(2));
            mock.getBean(MarketplaceStore.class).addOrder(new MarketplaceOrder(
                    "TY-IT",
                    System.currentTimeMillis() - 1_000,
                    "Ada",
                    "Lovelace",
                    List.of(new MarketplaceLine("BARCODE-MUG", "Stoneware mug", 1, 24900))));
            poller.pollOrders();
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(20));
            assertThat(records).isNotEmpty();
            assertThat(records.iterator().next().value()).contains("TY-IT");
        }
    }

    private static KafkaConsumer<String, String> consumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "importer-it-" + UUID.randomUUID());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new KafkaConsumer<>(properties);
    }
}
