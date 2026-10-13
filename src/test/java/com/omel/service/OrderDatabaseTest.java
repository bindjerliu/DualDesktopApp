package com.omel.service;

import com.omel.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDatabaseTest {
    @TempDir Path dir;
    Path file;
    OrderDatabase db;

    @BeforeEach
    void setUp() {
        file = dir.resolve("data").resolve("orders.txt");
        db = new OrderDatabase(file);
    }

    private Order create(String name, String total) {
        return db.create(name, name.toLowerCase().replace(' ', '.') + "@rit.edu", "12:30", "Still Water x1",
                new BigDecimal(total), "");
    }

    @Test
    void createsTheDatabaseFileAndFolder() {
        assertTrue(Files.isRegularFile(file));
    }

    @Test
    void idsCountUpFromOmel1001() {
        assertEquals("OMEL-1001", create("Ann One", "1.00").getId());
        assertEquals("OMEL-1002", create("Bob Two", "2.00").getId());
    }

    @Test
    void newOrdersStartAsPending() {
        assertEquals("PENDING", create("Ann One", "1.00").getStatus());
        assertEquals(1, db.findAll().size());
    }

    /** Regression: updating one order used to delete every line the app could not parse. */
    @Test
    void updateStatusNeverDropsOrReordersOtherLines() throws IOException {
        create("Ann One", "1.00");
        Files.writeString(file, "this line was edited by hand and is not a valid order\n",
                StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        create("Bob Two", "2.00");

        assertTrue(db.updateStatus("OMEL-1002", "READY"));

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(3, lines.size());
        assertTrue(lines.get(0).contains("OMEL-1001"));
        assertEquals("this line was edited by hand and is not a valid order", lines.get(1));
        assertTrue(lines.get(2).contains("OMEL-1002") && lines.get(2).contains("|READY|"));
    }

    @Test
    void updatingAnUnknownOrderChangesNothing() throws IOException {
        create("Ann One", "1.00");
        String before = Files.readString(file);
        assertFalse(db.updateStatus("OMEL-9999", "READY"));
        assertEquals(before, Files.readString(file));
    }

    @Test
    void unreadableLinesAreSkippedWhenReading() throws IOException {
        create("Ann One", "1.00");
        Files.writeString(file, "garbage\n\n", StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        assertEquals(1, db.findAll().size());
    }

    @Test
    void appendingAfterAFileWithoutTrailingNewlineDoesNotGlueLinesTogether() throws IOException {
        create("Ann One", "1.00");
        String content = Files.readString(file).stripTrailing();   // simulate a hand edit that removed the final newline
        Files.writeString(file, content, StandardCharsets.UTF_8);
        create("Bob Two", "2.00");
        assertEquals(2, db.findAll().size());
    }

    @Test
    void newestOrderIsListedFirst() {
        db.save(new Order("OMEL-1001", "2026-04-28 09:00", "A", "a@rit.edu", "12:00", "x", BigDecimal.ONE, "PENDING", ""));
        db.save(new Order("OMEL-1002", "2026-04-28 10:00", "B", "b@rit.edu", "12:00", "x", BigDecimal.ONE, "PENDING", ""));
        assertEquals("OMEL-1002", db.findAll().get(0).getId());
    }

    @Test
    void foreignIdsDoNotBreakNumbering() {
        db.save(new Order("OMEL-abc", "2026-04-28 09:00", "A", "a@rit.edu", "12:00", "x", BigDecimal.ONE, "PENDING", ""));
        db.save(new Order("OMEL-1005", "2026-04-28 09:01", "B", "b@rit.edu", "12:00", "x", BigDecimal.ONE, "PENDING", ""));
        assertEquals("OMEL-1006", db.nextOrderId());
    }
}
