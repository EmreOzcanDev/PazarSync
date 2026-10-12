package dev.emreozcan.pazarsync.importer;

import java.util.List;

public record RemoteOrder(
        String orderNumber,
        long orderDate,
        String customerFirstName,
        String customerLastName,
        List<RemoteLine> lines) {
}
