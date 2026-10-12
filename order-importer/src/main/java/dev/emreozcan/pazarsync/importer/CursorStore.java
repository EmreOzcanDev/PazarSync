package dev.emreozcan.pazarsync.importer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CursorStore {

    private final SyncCursorRepository repository;

    public CursorStore(SyncCursorRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Instant get(String stream) {
        return repository.findById(stream).map(SyncCursor::getCursorAt).orElse(Instant.EPOCH);
    }

    @Transactional
    public void advance(String stream, Instant cursorAt) {
        SyncCursor cursor = repository.findById(stream).orElseGet(() -> new SyncCursor(stream, cursorAt));
        cursor.setCursorAt(cursorAt);
        repository.save(cursor);
    }
}
