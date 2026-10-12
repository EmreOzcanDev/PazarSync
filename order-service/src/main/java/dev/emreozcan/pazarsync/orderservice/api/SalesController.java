package dev.emreozcan.pazarsync.orderservice.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
public class SalesController {

    private final JdbcTemplate jdbc;

    public SalesController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/sales")
    public List<DailySales> sales(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        LocalDate end = to == null ? LocalDate.now(ZoneOffset.UTC).plusDays(1) : to.plusDays(1);
        LocalDate start = from == null ? end.minusDays(15) : from;
        return jdbc.query("""
                SELECT CAST(ordered_at AS date) AS day,
                       COALESCE(SUM(total_minor), 0) AS total_minor,
                       COUNT(*) AS order_count
                FROM orders
                WHERE ordered_at >= ? AND ordered_at < ?
                GROUP BY CAST(ordered_at AS date)
                ORDER BY day
                """,
                (rs, row) -> new DailySales(
                        rs.getDate("day").toLocalDate(),
                        rs.getLong("total_minor"),
                        rs.getLong("order_count")),
                Timestamp.from(start.atStartOfDay().toInstant(ZoneOffset.UTC)),
                Timestamp.from(end.atStartOfDay().toInstant(ZoneOffset.UTC)));
    }

    public record DailySales(LocalDate day, long totalMinor, long orderCount) {
    }
}
