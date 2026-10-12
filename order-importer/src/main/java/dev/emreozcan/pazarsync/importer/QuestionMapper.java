package dev.emreozcan.pazarsync.importer;

import dev.emreozcan.pazarsync.events.QuestionReceived;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class QuestionMapper {

    public QuestionReceived toEvent(String marketplace, RemoteQuestion question) {
        return new QuestionReceived(
                marketplace,
                Long.toString(question.id()),
                question.productBarcode(),
                question.userName(),
                question.text(),
                Instant.ofEpochMilli(question.creationDate()));
    }
}
