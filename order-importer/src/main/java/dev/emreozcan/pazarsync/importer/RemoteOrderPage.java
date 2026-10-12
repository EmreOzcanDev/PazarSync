package dev.emreozcan.pazarsync.importer;

import java.util.List;

public record RemoteOrderPage(List<RemoteOrder> content, int totalElements) {
}
