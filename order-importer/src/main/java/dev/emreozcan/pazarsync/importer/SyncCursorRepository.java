package dev.emreozcan.pazarsync.importer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncCursorRepository extends JpaRepository<SyncCursor, String> {
}
