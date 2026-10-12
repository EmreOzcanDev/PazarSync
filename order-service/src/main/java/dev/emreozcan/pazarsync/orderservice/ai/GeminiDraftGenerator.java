package dev.emreozcan.pazarsync.orderservice.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("gemini")
public class GeminiDraftGenerator implements DraftGenerator {

    private final ChatClient chatClient;

    public GeminiDraftGenerator(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public String generate(String question, String productName, List<String> similarAnswers) {
        String examples = similarAnswers == null || similarAnswers.isEmpty()
                ? "No past answers."
                : String.join("\n", similarAnswers);
        return chatClient.prompt()
                .system("You draft short, polite marketplace seller replies. Use past answers when they apply. Return only the reply text.")
                .user("Product: " + productName + "\nQuestion: " + question + "\nPast answers:\n" + examples)
                .call()
                .content();
    }
}
