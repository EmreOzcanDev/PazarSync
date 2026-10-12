package dev.emreozcan.pazarsync.orderservice.ai;

import dev.emreozcan.pazarsync.events.AnswerApproved;
import dev.emreozcan.pazarsync.events.EventCodec;
import dev.emreozcan.pazarsync.events.EventKeys;
import dev.emreozcan.pazarsync.events.KafkaTopics;
import dev.emreozcan.pazarsync.orderservice.domain.AnswerDraft;
import dev.emreozcan.pazarsync.orderservice.domain.AnswerDraftRepository;
import dev.emreozcan.pazarsync.orderservice.domain.CustomerQuestion;
import dev.emreozcan.pazarsync.orderservice.domain.CustomerQuestionRepository;
import dev.emreozcan.pazarsync.orderservice.domain.ProductRepository;
import dev.emreozcan.pazarsync.orderservice.ingest.DraftCoordinator;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class QuestionDraftService implements DraftCoordinator {

    private final CustomerQuestionRepository questions;
    private final AnswerDraftRepository drafts;
    private final ProductRepository products;
    private final DraftGenerator generator;
    private final VectorStore vectorStore;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public QuestionDraftService(
            CustomerQuestionRepository questions,
            AnswerDraftRepository drafts,
            ProductRepository products,
            DraftGenerator generator,
            VectorStore vectorStore,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.questions = questions;
        this.drafts = drafts;
        this.products = products;
        this.generator = generator;
        this.vectorStore = vectorStore;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void onQuestionStored(CustomerQuestion question) {
        String productName = products.findByBarcode(question.getProductBarcode())
                .map(product -> product.getName())
                .orElse(question.getProductBarcode());
        List<String> similar = vectorStore.similaritySearch(SearchRequest.builder()
                        .query(question.getQuestionText())
                        .topK(3)
                        .build())
                .stream()
                .map(Document::getText)
                .toList();
        String text = generator.generate(question.getQuestionText(), productName, similar);
        AnswerDraft draft = new AnswerDraft();
        draft.setId(UUID.randomUUID());
        draft.setQuestionId(question.getId());
        draft.setDraftText(text);
        draft.setUpdatedAt(Instant.now());
        drafts.save(draft);
        question.setStatus("DRAFTED");
        questions.save(question);
    }

    @Transactional
    public CustomerQuestion updateDraft(UUID questionId, String text) {
        CustomerQuestion question = require(questionId);
        AnswerDraft draft = requireDraft(questionId);
        draft.setDraftText(text);
        draft.setUpdatedAt(Instant.now());
        return question;
    }

    @Transactional
    public CustomerQuestion approve(UUID questionId, String text) {
        CustomerQuestion question = require(questionId);
        AnswerDraft draft = requireDraft(questionId);
        draft.setDraftText(text);
        draft.setUpdatedAt(Instant.now());
        question.setStatus("APPROVED");
        vectorStore.add(List.of(new Document(
                question.getQuestionText() + "\n" + text,
                Map.of("questionId", questionId.toString()))));
        String payload = EventCodec.write(new AnswerApproved(
                question.getMarketplace(),
                question.getExternalQuestionId(),
                text));
        String key = EventKeys.of(question.getMarketplace(), question.getExternalQuestionId());
        afterCommit(() -> kafkaTemplate.send(KafkaTopics.ANSWERS_APPROVED, key, payload));
        return question;
    }

    private CustomerQuestion require(UUID questionId) {
        return questions.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
    }

    private AnswerDraft requireDraft(UUID questionId) {
        return drafts.findByQuestionId(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Draft not found"));
    }

    private static void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
