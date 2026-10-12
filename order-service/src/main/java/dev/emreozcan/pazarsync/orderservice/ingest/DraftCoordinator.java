package dev.emreozcan.pazarsync.orderservice.ingest;

import dev.emreozcan.pazarsync.orderservice.domain.CustomerQuestion;

public interface DraftCoordinator {

    void onQuestionStored(CustomerQuestion question);
}
