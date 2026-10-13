package com.omel.service;

import com.omel.model.Order;
import com.omel.model.OrderStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orders live in a plain text file, one pipe-separated order per line.
 * <p>
 * Guarantees: a line that cannot be parsed is skipped when reading and is <b>never</b> rewritten or
 * dropped when another order is updated; rewrites go through a temp file so a crash cannot leave a
 * half-written database. Access is synchronized across all instances in this JVM.
 */
public class OrderDatabase {
    private static final Logger LOG = Logger.getLogger(OrderDatabase.class.getName());
    private static final Path DEFAULT_PATH = Path.of("data", "orders.txt");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Object LOCK = new Object();

    private final Path dbPath;

    public OrderDatabase() {
        this(DEFAULT_PATH);
    }

    public OrderDatabase(Path dbPath) {
        this.dbPath = dbPath;
        synchronized (LOCK) {
            ensureDatabase();
        }
    }

    /** Creates a PENDING order with the next free id and the current time, and stores it. */
    public Order create(String customerName, String customerEmail, String pickupTime,
                        String items, BigDecimal total, String notes) {
        synchronized (LOCK) {
            Order order = new Order(nextOrderId(), now(), customerName, customerEmail, pickupTime,
                    items, total, OrderStatus.PENDING.name(), notes);
            save(order);
            return order;
        }
    }

    public void save(Order order) {
        synchronized (LOCK) {
            ensureDatabase();
            try {
                String prefix = endsWithoutNewline() ? System.lineSeparator() : "";
                Files.writeString(dbPath, prefix + order.serialize() + System.lineSeparator(), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("Could not save order to text database.", e);
            }
        }
    }

    /** All readable orders, newest first. Unreadable lines are skipped (and logged by line number). */
    public List<Order> findAll() {
        synchronized (LOCK) {
            ensureDatabase();
            List<String> lines = readLines();
            List<Order> orders = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).isBlank()) continue;
                Order order = tryParse(lines.get(i));
                if (order == null) {
                    LOG.warning("Skipping unreadable order record at line " + (i + 1) + " of " + dbPath);
                } else {
                    orders.add(order);
                }
            }
            orders.sort(Comparator.comparing(Order::getTimestamp).reversed());
            return orders;
        }
    }

    /**
     * Changes one order's status. Every other line - including ones this program cannot parse -
     * is written back exactly as it was, in its original position.
     *
     * @return true if the order was found
     */
    public boolean updateStatus(String orderId, String newStatus) {
        synchronized (LOCK) {
            ensureDatabase();
            List<String> lines = readLines();
            List<String> out = new ArrayList<>(lines.size());
            boolean changed = false;
            for (String line : lines) {
                Order order = line.isBlank() ? null : tryParse(line);
                if (order != null && order.getId().equals(orderId)) {
                    order.setStatus(newStatus);
                    out.add(order.serialize());
                    changed = true;
                } else {
                    out.add(line);
                }
            }
            if (changed) writeAtomically(out);
            return changed;
        }
    }

    public String nextOrderId() {
        synchronized (LOCK) {
            int max = 1000;
            for (Order order : findAll()) {
                String id = order.getId();
                if (!id.startsWith("OMEL-")) continue;
                try {
                    max = Math.max(max, Integer.parseInt(id.substring("OMEL-".length())));
                } catch (NumberFormatException ignored) {
                    // not one of ours; ids like OMEL-abc do not influence numbering
                }
            }
            return "OMEL-" + (max + 1);
        }
    }

    public String now() {
        return LocalDateTime.now().format(FORMATTER);
    }

    // ---- internals ----

    private static Order tryParse(String line) {
        try {
            return Order.fromLine(line);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private List<String> readLines() {
        try {
            return Files.readAllLines(dbPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not read orders from text database.", e);
        }
    }

    private void writeAtomically(List<String> lines) {
        Path tmp = dbPath.resolveSibling(dbPath.getFileName() + ".tmp");
        try {
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            try {
                Files.move(tmp, dbPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, dbPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException cleanup) {
                LOG.log(Level.FINE, "Could not delete temp file " + tmp, cleanup);
            }
            throw new RuntimeException("Could not update text database.", e);
        }
    }

    /** True when the file has content but its last byte is not a line break (e.g. after a manual edit). */
    private boolean endsWithoutNewline() throws IOException {
        if (!Files.exists(dbPath) || Files.size(dbPath) == 0) return false;
        try (SeekableByteChannel ch = Files.newByteChannel(dbPath, StandardOpenOption.READ)) {
            ch.position(ch.size() - 1);
            ByteBuffer last = ByteBuffer.allocate(1);
            ch.read(last);
            byte b = last.get(0);
            return b != '\n' && b != '\r';
        }
    }

    private void ensureDatabase() {
        try {
            Path parent = dbPath.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            if (!Files.exists(dbPath)) Files.createFile(dbPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create " + dbPath, e);
        }
    }
}
