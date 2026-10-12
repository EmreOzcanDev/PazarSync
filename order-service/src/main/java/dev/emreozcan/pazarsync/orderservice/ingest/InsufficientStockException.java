package dev.emreozcan.pazarsync.orderservice.ingest;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String barcode) {
        super("Not enough stock for " + barcode);
    }
}
