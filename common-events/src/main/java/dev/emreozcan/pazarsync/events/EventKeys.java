package dev.emreozcan.pazarsync.events;

public final class EventKeys {

    private EventKeys() {
    }

    public static String of(String marketplace, String id) {
        return marketplace + ":" + id;
    }
}
