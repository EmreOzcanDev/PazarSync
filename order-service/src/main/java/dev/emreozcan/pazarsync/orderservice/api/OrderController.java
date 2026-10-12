package dev.emreozcan.pazarsync.orderservice.api;

import dev.emreozcan.pazarsync.orderservice.domain.CustomerOrder;
import dev.emreozcan.pazarsync.orderservice.domain.CustomerOrderRepository;
import dev.emreozcan.pazarsync.orderservice.domain.OrderLine;
import dev.emreozcan.pazarsync.orderservice.domain.OrderLineRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class OrderController {

    private final CustomerOrderRepository orders;
    private final OrderLineRepository lines;

    public OrderController(CustomerOrderRepository orders, OrderLineRepository lines) {
        this.orders = orders;
        this.lines = lines;
    }

    @GetMapping("/api/orders")
    public List<OrderResponse> list() {
        List<CustomerOrder> recent = orders.findTop100ByOrderByOrderedAtDesc();
        if (recent.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<OrderLine>> byOrder = lines.findByOrderIdIn(recent.stream().map(CustomerOrder::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(OrderLine::getOrderId));
        return recent.stream()
                .map(order -> new OrderResponse(
                        order.getId(),
                        order.getMarketplace(),
                        order.getExternalOrderId(),
                        order.getCustomerName(),
                        order.getOrderedAt(),
                        order.getTotalMinor(),
                        byOrder.getOrDefault(order.getId(), List.of()).stream()
                                .map(line -> new Line(
                                        line.getBarcode(),
                                        line.getProductName(),
                                        line.getQuantity(),
                                        line.getUnitPriceMinor()))
                                .toList()))
                .toList();
    }

    public record OrderResponse(
            UUID id,
            String marketplace,
            String externalOrderId,
            String customerName,
            Instant orderedAt,
            long totalMinor,
            List<Line> lines) {
    }

    public record Line(String barcode, String productName, int quantity, long unitPriceMinor) {
    }
}
