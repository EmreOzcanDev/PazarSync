package dev.emreozcan.pazarsync.orderservice.ingest;

public class EventReadException extends RuntimeException {

    public EventReadException(Throwable cause) {
        super("Unreadable event", cause);
    }
}
