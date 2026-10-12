package dev.emreozcan.pazarsync.orderservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StoredSyncFailureRepository extends JpaRepository<StoredSyncFailure, UUID> {

    List<StoredSyncFailure> findByResolvedAtIsNullOrderByCreatedAtDesc();
}
