package dev.emreozcan.pazarsync.orderservice.ai;

import java.util.List;

public interface DraftGenerator {

    String generate(String question, String productName, List<String> similarAnswers);
}
