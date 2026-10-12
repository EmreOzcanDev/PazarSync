package dev.emreozcan.pazarsync.orderservice.api;

import dev.emreozcan.pazarsync.orderservice.domain.ProductRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StockController {

    private final ProductRepository products;

    public StockController(ProductRepository products) {
        this.products = products;
    }

    @GetMapping("/api/stock/low")
    public List<LowStockResponse> lowStock() {
        return products.findByLowStockTrueOrderByQuantityAsc().stream()
                .map(product -> new LowStockResponse(
                        product.getBarcode(),
                        product.getName(),
                        product.getQuantity(),
                        product.getLowStockThreshold(),
                        product.getSalePriceMinor()))
                .toList();
    }

    public record LowStockResponse(String barcode, String name, int quantity, int threshold, long salePriceMinor) {
    }
}
