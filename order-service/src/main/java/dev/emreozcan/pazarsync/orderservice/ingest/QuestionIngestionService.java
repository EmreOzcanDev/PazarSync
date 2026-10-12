package dev.emreozcan.pazarsync.orderservice.ingest;

import dev.emreozcan.pazarsync.events.QuestionReceived;
import dev.emreozcan.pazarsync.orderservice.domain.CustomerQuestion;
import dev.emreozcan.pazarsync.orderservice.domain.CustomerQuestionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.UUID;

@Service
public class QuestionIngestionService {

    private final JdbcTemplate jdbc;
    private final CustomerQuestionRepository questions;
    private final DraftCoordinator drafts;

    public QuestionIngestionService(
            JdbcTemplate jdbc,
            CustomerQuestionRepository questions,
            DraftCoordinator drafts) {
        this.jdbc = jdbc;
        this.questions = questions;
        this.drafts = drafts;
    }

    @Transactional
    public IngestResult ingest(QuestionReceived event) {
        UUID id = UUID.randomUUID();
        int inserted = jdbc.update("""
                INSERT INTO questions (
                    id, marketplace, external_question_id, product_barcode, customer_name, question_text, asked_at, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'OPEN')
                ON CONFLICT (marketplace, external_question_id) DO NOTHING
                """,
                id,
                event.marketplace(),
                event.externalQuestionId(),
                event.productBarcode(),
                event.customerName(),
                event.text(),
                Timestamp.from(event.askedAt()));
        if (inserted == 0) {
            return IngestResult.DUPLICATE;
        }
        CustomerQuestion question = questions.findById(id).orElseThrow();
        drafts.onQuestionStored(question);
        return IngestResult.CREATED;
    }
}
