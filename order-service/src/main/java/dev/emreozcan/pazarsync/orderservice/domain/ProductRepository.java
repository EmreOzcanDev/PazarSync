package dev.emreozcan.pazarsync.orderservice.domain;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByBarcode(String barcode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.barcode = :barcode")
    Optional<Product> lockByBarcode(@Param("barcode") String barcode);

    List<Product> findByLowStockTrueOrderByQuantityAsc();
}
