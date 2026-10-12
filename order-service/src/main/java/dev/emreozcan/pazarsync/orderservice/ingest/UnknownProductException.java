package dev.emreozcan.pazarsync.orderservice.ingest;

public class UnknownProductException extends RuntimeException {

    public UnknownProductException(String barcode) {
        super("Unknown product " + barcode);
    }
}
