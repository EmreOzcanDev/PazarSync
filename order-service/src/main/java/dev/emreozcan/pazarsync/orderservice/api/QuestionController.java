package dev.emreozcan.pazarsync.orderservice.api;

import dev.emreozcan.pazarsync.orderservice.ai.QuestionDraftService;
import dev.emreozcan.pazarsync.orderservice.domain.AnswerDraft;
import dev.emreozcan.pazarsync.orderservice.domain.AnswerDraftRepository;
import dev.emreozcan.pazarsync.orderservice.domain.CustomerQuestion;
import dev.emreozcan.pazarsync.orderservice.domain.CustomerQuestionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class QuestionController {

    private final CustomerQuestionRepository questions;
    private final AnswerDraftRepository drafts;
    private final QuestionDraftService draftService;

    public QuestionController(
            CustomerQuestionRepository questions,
            AnswerDraftRepository drafts,
            QuestionDraftService draftService) {
        this.questions = questions;
        this.drafts = drafts;
        this.draftService = draftService;
    }

    @GetMapping("/api/questions")
    public List<QuestionResponse> list() {
        List<CustomerQuestion> recent = questions.findTop100ByOrderByAskedAtDesc();
        if (recent.isEmpty()) {
            return List.of();
        }
        Map<UUID, String> draftText = drafts.findByQuestionIdIn(recent.stream().map(CustomerQuestion::getId).toList())
                .stream()
                .collect(Collectors.toMap(AnswerDraft::getQuestionId, AnswerDraft::getDraftText));
        return recent.stream()
                .map(question -> new QuestionResponse(
                        question.getId(),
                        question.getMarketplace(),
                        question.getExternalQuestionId(),
                        question.getProductBarcode(),
                        question.getCustomerName(),
                        question.getQuestionText(),
                        question.getAskedAt(),
                        question.getStatus(),
                        draftText.get(question.getId())))
                .toList();
    }

    @PutMapping("/api/questions/{id}/draft")
    public QuestionResponse updateDraft(@PathVariable UUID id, @RequestBody TextRequest request) {
        CustomerQuestion question = draftService.updateDraft(id, request.text());
        return toResponse(question, request.text());
    }

    @PostMapping("/api/questions/{id}/approve")
    public QuestionResponse approve(@PathVariable UUID id, @RequestBody TextRequest request) {
        CustomerQuestion question = draftService.approve(id, request.text());
        return toResponse(question, request.text());
    }

    private static QuestionResponse toResponse(CustomerQuestion question, String draft) {
        return new QuestionResponse(
                question.getId(),
                question.getMarketplace(),
                question.getExternalQuestionId(),
                question.getProductBarcode(),
                question.getCustomerName(),
                question.getQuestionText(),
                question.getAskedAt(),
                question.getStatus(),
                draft);
    }

    public record TextRequest(String text) {
    }

    public record QuestionResponse(
            UUID id,
            String marketplace,
            String externalQuestionId,
            String productBarcode,
            String customerName,
            String text,
            Instant askedAt,
            String status,
            String draftText) {
    }
}
