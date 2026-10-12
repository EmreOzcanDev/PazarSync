package dev.emreozcan.pazarsync.orderservice.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DraftGeneratorTest {

    @Test
    void stubMentionsTheProductAndQuestion() {
        String draft = new StubDraftGenerator().generate("Is it dishwasher safe?", "Stoneware mug", List.of());

        assertTrue(draft.contains("Stoneware mug"));
        assertTrue(draft.contains("dishwasher safe"));
    }

    @Test
    void geminiDraftUsesTheChatClient() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec request = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec call = mock(ChatClient.CallResponseSpec.class);
        when(chatClient.prompt()).thenReturn(request);
        when(request.system(anyString())).thenReturn(request);
        when(request.user(anyString())).thenReturn(request);
        when(request.call()).thenReturn(call);
        when(call.content()).thenReturn("Ships tomorrow.");
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(chatClient);

        String draft = new GeminiDraftGenerator(builder).generate("When will it ship?", "Stoneware mug", List.of());

        assertEquals("Ships tomorrow.", draft);
    }
}
