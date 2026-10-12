package dev.emreozcan.pazarsync.orderservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerQuestionRepository extends JpaRepository<CustomerQuestion, UUID> {

    List<CustomerQuestion> findTop100ByOrderByAskedAtDesc();
}
