package dev.emreozcan.pazarsync.mock;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.DoubleSupplier;

@Component
public class MarketplaceStore {

    private final MockMarketplaceProperties properties;
    private final DoubleSupplier random;
    private final ConcurrentMap<String, MarketplaceOrder> orders = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, MarketplaceQuestion> questions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, StockItem> stock = new ConcurrentHashMap<>();

    public MarketplaceStore(MockMarketplaceProperties properties) {
        this(properties, Math::random);
    }

    MarketplaceStore(MockMarketplaceProperties properties, DoubleSupplier random) {
        this.properties = properties;
        this.random = random;
        for (Catalog.Product product : Catalog.PRODUCTS) {
            stock.put(product.barcode(), new StockItem(product.barcode(), product.quantity(), product.salePrice()));
        }
    }

    public void addOrder(MarketplaceOrder order) {
        orders.put(order.orderNumber(), order);
    }

    public void addQuestion(MarketplaceQuestion question) {
        questions.put(question.id(), question);
    }

    public List<MarketplaceOrder> ordersBetween(long startInclusive, long endExclusive) {
        List<MarketplaceOrder> found = orders.values().stream()
                .filter(order -> order.orderDate() >= startInclusive && order.orderDate() < endExclusive)
                .sorted(Comparator.comparingLong(MarketplaceOrder::orderDate))
                .toList();
        if (duplicateThisPage() && !found.isEmpty()) {
            List<MarketplaceOrder> withDuplicate = new ArrayList<>(found);
            withDuplicate.add(found.getFirst());
            return withDuplicate;
        }
        return found;
    }

    public List<MarketplaceQuestion> questionsBetween(long startInclusive, long endExclusive) {
        List<MarketplaceQuestion> found = questions.values().stream()
                .filter(question -> question.creationDate() >= startInclusive && question.creationDate() < endExclusive)
                .sorted(Comparator.comparingLong(MarketplaceQuestion::creationDate))
                .toList();
        if (duplicateThisPage() && !found.isEmpty()) {
            List<MarketplaceQuestion> withDuplicate = new ArrayList<>(found);
            withDuplicate.add(found.getFirst());
            return withDuplicate;
        }
        return found;
    }

    public void updateStock(String barcode, int quantity, long salePrice) {
        stock.compute(barcode, (key, current) -> new StockItem(barcode, quantity, salePrice));
    }

    public StockItem stock(String barcode) {
        return stock.get(barcode);
    }

    public MarketplaceQuestion answer(long questionId, String text) {
        return questions.compute(questionId, (id, current) -> {
            if (current == null) {
                return null;
            }
            return new MarketplaceQuestion(
                    current.id(),
                    current.productBarcode(),
                    current.productName(),
                    current.userName(),
                    current.text(),
                    current.creationDate(),
                    "ANSWERED",
                    text);
        });
    }

    private boolean duplicateThisPage() {
        MockMarketplaceProperties.Faults faults = properties.getFaults();
        return faults.isEnabled() && random.getAsDouble() < faults.getDuplicateRate();
    }

    public record StockItem(String barcode, int quantity, long salePrice) {
    }
}
