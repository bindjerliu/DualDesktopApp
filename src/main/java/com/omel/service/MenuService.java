package com.omel.service;

import com.omel.model.MenuItem;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** The cafeteria menu. Which items are switched off is remembered between runs in a small state file. */
public class MenuService {
    private static final Logger LOG = Logger.getLogger(MenuService.class.getName());
    private static final Path DEFAULT_STATE_FILE = Path.of("data", "menu-state.txt");

    private final List<MenuItem> items = new ArrayList<>();
    private final Path stateFile;

    public MenuService() {
        this(DEFAULT_STATE_FILE);
    }

    public MenuService(Path stateFile) {
        this.stateFile = stateFile;
        items.addAll(defaultMenu());
        loadAvailability();
    }

    public List<MenuItem> getAllItems() {
        return Collections.unmodifiableList(items);
    }

    public List<MenuItem> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAllItems();
        String k = keyword.toLowerCase();
        return items.stream()
                .filter(i -> i.getName().toLowerCase().contains(k) || i.getCategory().toLowerCase().contains(k)
                        || i.getDescription().toLowerCase().contains(k) || i.getDietary().toLowerCase().contains(k))
                .collect(Collectors.toList());
    }

    public long availableCount() {
        return items.stream().filter(MenuItem::isAvailable).count();
    }

    /** Switches an item on or off and remembers the choice for the next run. */
    public void setAvailable(MenuItem item, boolean available) {
        item.setAvailable(available);
        saveAvailability();
    }

    private void loadAvailability() {
        if (!Files.isRegularFile(stateFile)) return;
        try {
            Set<String> unavailable = new HashSet<>(Files.readAllLines(stateFile, StandardCharsets.UTF_8));
            for (MenuItem item : items) {
                if (unavailable.contains(item.getName())) item.setAvailable(false);
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not read menu state; all items stay available.", e);
        }
    }

    private void saveAvailability() {
        try {
            Path parent = stateFile.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            List<String> unavailable = items.stream().filter(i -> !i.isAvailable()).map(MenuItem::getName).toList();
            Files.write(stateFile, unavailable, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not save menu state; the change only lasts until the app closes.", e);
        }
    }

    private static List<MenuItem> defaultMenu() {
        return List.of(
                item("Morning Brew Coffee", "Coffee", "2.00", "Freshly brewed campus coffee with a smooth finish.", "Vegetarian"),
                item("Iced Caramel Latte", "Coffee", "2.80", "Chilled espresso, milk and caramel drizzle.", "Vegetarian"),
                item("Matcha Cloud Latte", "Coffee", "3.20", "Ceremonial matcha with milk and vanilla foam.", "Vegetarian"),
                item("Chicken Caesar Wrap", "Meals", "3.70", "Grilled chicken, lettuce, parmesan and caesar sauce.", "High protein"),
                item("Mediterranean Panini", "Meals", "3.40", "Mozzarella, tomato, pesto and grilled vegetables.", "Vegetarian"),
                item("Omel Energy Bowl", "Meals", "4.20", "Rice, chicken, avocado, corn and house dressing.", "High protein"),
                item("Protein Yogurt Cup", "Snacks", "2.30", "Greek yogurt with berries and granola.", "High protein"),
                item("Chocolate Croissant", "Snacks", "1.80", "Buttery croissant filled with chocolate.", "Vegetarian"),
                item("Fresh Orange Juice", "Drinks", "2.50", "Cold pressed orange juice.", "Vegan"),
                item("Still Water", "Drinks", "1.00", "500ml bottled water.", "Vegan"));
    }

    private static MenuItem item(String name, String category, String price, String description, String dietary) {
        return new MenuItem(name, category, new BigDecimal(price), description, dietary, true);
    }
}
