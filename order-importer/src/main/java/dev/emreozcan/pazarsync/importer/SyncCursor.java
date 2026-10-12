package dev.emreozcan.pazarsync.importer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "sync_cursor", schema = "importer")
public class SyncCursor {

    @Id
    private String stream;

    @Column(name = "cursor_at", nullable = false)
    private Instant cursorAt;

    protected SyncCursor() {
    }

    public SyncCursor(String stream, Instant cursorAt) {
        this.stream = stream;
        this.cursorAt = cursorAt;
    }

    public String getStream() {
        return stream;
    }

    public Instant getCursorAt() {
        return cursorAt;
    }

    public void setCursorAt(Instant cursorAt) {
        this.cursorAt = cursorAt;
    }
}
