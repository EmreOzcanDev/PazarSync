package dev.emreozcan.pazarsync.orderservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders", uniqueConstraints = @UniqueConstraint(name = "uq_orders_external", columnNames = {"marketplace", "external_order_id"}))
public class CustomerOrder {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String marketplace;

    @Column(name = "external_order_id", nullable = false)
    private String externalOrderId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "ordered_at", nullable = false)
    private Instant orderedAt;

    @Column(name = "total_minor", nullable = false)
    private long totalMinor;

    public UUID getId() {
        return id;
    }

    public String getMarketplace() {
        return marketplace;
    }

    public String getExternalOrderId() {
        return externalOrderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Instant getOrderedAt() {
        return orderedAt;
    }

    public long getTotalMinor() {
        return totalMinor;
    }
}
