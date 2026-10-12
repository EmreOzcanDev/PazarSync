package dev.emreozcan.pazarsync.importer;

import java.util.List;

public record RemoteQuestionPage(List<RemoteQuestion> content, int totalElements) {
}
