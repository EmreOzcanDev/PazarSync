package dev.emreozcan.pazarsync.orderservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnswerDraftRepository extends JpaRepository<AnswerDraft, UUID> {

    Optional<AnswerDraft> findByQuestionId(UUID questionId);

    List<AnswerDraft> findByQuestionIdIn(Collection<UUID> questionIds);
}
