package dev.emreozcan.pazarsync.mock;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class MarketplaceController {

    private final MarketplaceStore store;

    public MarketplaceController(MarketplaceStore store) {
        this.store = store;
    }

    @GetMapping("/suppliers/{supplierId}/orders")
    public OrderPage orders(
            @PathVariable long supplierId,
            @RequestParam long startDate,
            @RequestParam long endDate) {
        List<MarketplaceOrder> content = store.ordersBetween(startDate, endDate);
        return new OrderPage(content, content.size());
    }

    @GetMapping("/suppliers/{supplierId}/questions")
    public QuestionPage questions(
            @PathVariable long supplierId,
            @RequestParam long startDate,
            @RequestParam long endDate) {
        List<MarketplaceQuestion> content = store.questionsBetween(startDate, endDate);
        return new QuestionPage(content, content.size());
    }

    @PutMapping("/suppliers/{supplierId}/products/price-and-inventory")
    public void updatePriceAndInventory(
            @PathVariable long supplierId,
            @RequestBody PriceAndInventoryRequest request) {
        if (request.items() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "items are required");
        }
        for (PriceAndInventoryRequest.Item item : request.items()) {
            store.updateStock(item.barcode(), item.quantity(), item.salePrice());
        }
    }

    @PostMapping("/suppliers/{supplierId}/questions/{questionId}/answers")
    public MarketplaceQuestion answer(
            @PathVariable long supplierId,
            @PathVariable long questionId,
            @RequestBody AnswerRequest request) {
        MarketplaceQuestion updated = store.answer(questionId, request.text());
        if (updated == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        }
        return updated;
    }

    public record OrderPage(List<MarketplaceOrder> content, int totalElements) {
    }

    public record QuestionPage(List<MarketplaceQuestion> content, int totalElements) {
    }

    public record PriceAndInventoryRequest(List<Item> items) {
        public record Item(String barcode, int quantity, long salePrice) {
        }
    }

    public record AnswerRequest(String text) {
    }
}
