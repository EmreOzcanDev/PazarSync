package dev.emreozcan.pazarsync.orderservice.ai;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("ai-stub")
public class StubDraftGenerator implements DraftGenerator {

    @Override
    public String generate(String question, String productName, List<String> similarAnswers) {
        String similar = similarAnswers == null || similarAnswers.isEmpty()
                ? ""
                : " Similar past answer: " + similarAnswers.getFirst();
        return "Thanks for your question about " + productName + ". We'll help with: " + question + "." + similar;
    }
}
