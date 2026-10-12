package dev.emreozcan.pazarsync.orderservice.ai;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@Profile("ai-stub")
public class HashEmbeddingModel implements EmbeddingModel {

    static final int DIMENSIONS = 32;

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        List<String> inputs = request.getInstructions();
        for (int index = 0; index < inputs.size(); index++) {
            embeddings.add(new Embedding(embedText(inputs.get(index)), index));
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return embedText(document.getText());
    }

    @Override
    public int dimensions() {
        return DIMENSIONS;
    }

    private static float[] embedText(String text) {
        float[] vector = new float[DIMENSIONS];
        if (text == null) {
            return vector;
        }
        for (String token : text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (token.length() < 3) {
                continue;
            }
            vector[Math.floorMod(token.hashCode(), DIMENSIONS)] += 1f;
        }
        float norm = 0f;
        for (float value : vector) {
            norm += value * value;
        }
        norm = (float) Math.sqrt(norm);
        if (norm == 0f) {
            return vector;
        }
        for (int index = 0; index < vector.length; index++) {
            vector[index] /= norm;
        }
        return vector;
    }
}
