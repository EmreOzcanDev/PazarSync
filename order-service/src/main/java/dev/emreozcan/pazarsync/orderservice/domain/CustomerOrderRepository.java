package dev.emreozcan.pazarsync.orderservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {

    List<CustomerOrder> findTop100ByOrderByOrderedAtDesc();

    long countByMarketplaceAndExternalOrderId(String marketplace, String externalOrderId);
}
