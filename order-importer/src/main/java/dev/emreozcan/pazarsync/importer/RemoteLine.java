package dev.emreozcan.pazarsync.importer;

public record RemoteLine(String barcode, String productName, int quantity, long price) {
}
