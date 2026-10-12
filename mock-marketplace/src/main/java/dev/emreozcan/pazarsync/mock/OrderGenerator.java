package dev.emreozcan.pazarsync.mock;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class OrderGenerator {

    private static final List<String> FIRST_NAMES = List.of("Ada", "Can", "Ece", "Deniz", "Mert", "Selin");
    private static final List<String> LAST_NAMES = List.of("Yilmaz", "Demir", "Kaya", "Aydin", "Celik");
    private static final List<String> QUESTION_TEXTS = List.of(
            "When will this ship?",
            "Is the mug dishwasher safe?",
            "Do you have this tee in another colour?",
            "What is the bag made of?");

    private final MarketplaceStore store;
    private final MockMarketplaceProperties properties;
    private final AtomicLong orderSequence = new AtomicLong(1000);
    private final AtomicLong questionSequence = new AtomicLong(1);

    public OrderGenerator(MarketplaceStore store, MockMarketplaceProperties properties) {
        this.store = store;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${pazarsync.mock.generator.delay-ms:4000}")
    public void tick() {
        if (!properties.getGenerator().isEnabled()) {
            return;
        }
        if (ThreadLocalRandom.current().nextInt(3) == 0) {
            generateQuestion();
        } else {
            generateOrder();
        }
    }

    public MarketplaceOrder generateOrder() {
        Catalog.Product product = Catalog.PRODUCTS.get(ThreadLocalRandom.current().nextInt(Catalog.PRODUCTS.size()));
        int quantity = 1 + ThreadLocalRandom.current().nextInt(2);
        long now = System.currentTimeMillis();
        MarketplaceOrder order = new MarketplaceOrder(
                "TY" + orderSequence.incrementAndGet(),
                now,
                FIRST_NAMES.get(ThreadLocalRandom.current().nextInt(FIRST_NAMES.size())),
                LAST_NAMES.get(ThreadLocalRandom.current().nextInt(LAST_NAMES.size())),
                List.of(new MarketplaceLine(product.barcode(), product.name(), quantity, product.salePrice())));
        store.addOrder(order);
        return order;
    }

    public MarketplaceQuestion generateQuestion() {
        Catalog.Product product = Catalog.PRODUCTS.get(ThreadLocalRandom.current().nextInt(Catalog.PRODUCTS.size()));
        MarketplaceQuestion question = new MarketplaceQuestion(
                questionSequence.incrementAndGet(),
                product.barcode(),
                product.name(),
                FIRST_NAMES.get(ThreadLocalRandom.current().nextInt(FIRST_NAMES.size())),
                QUESTION_TEXTS.get(ThreadLocalRandom.current().nextInt(QUESTION_TEXTS.size())),
                System.currentTimeMillis(),
                "WAITING",
                null);
        store.addQuestion(question);
        return question;
    }
}
