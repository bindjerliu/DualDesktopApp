package com.omel.service;

import com.omel.model.MenuItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MenuServiceTest {
    @TempDir Path dir;

    private MenuItem find(MenuService menu, String name) {
        return menu.getAllItems().stream().filter(i -> i.getName().equals(name)).findFirst().orElseThrow();
    }

    @Test
    void searchMatchesNameCategoryDescriptionAndDietary() {
        MenuService menu = new MenuService(dir.resolve("state.txt"));
        assertEquals(menu.getAllItems().size(), menu.search("").size());
        assertEquals(1, menu.search("matcha").size());
        assertEquals(3, menu.search("coffee").size());
        assertTrue(menu.search("vegan").stream().allMatch(i -> i.getDietary().equals("Vegan")));
        assertTrue(menu.search("zzz-no-such-thing").isEmpty());
    }

    /** Regression: switching an item off used to be forgotten when the app restarted. */
    @Test
    void unavailableItemsStayUnavailableAfterARestart() {
        Path state = dir.resolve("data").resolve("state.txt");
        MenuService first = new MenuService(state);
        first.setAvailable(find(first, "Still Water"), false);
        assertEquals(9, first.availableCount());

        MenuService second = new MenuService(state);
        assertFalse(find(second, "Still Water").isAvailable());
        assertEquals(9, second.availableCount());

        second.setAvailable(find(second, "Still Water"), true);
        assertEquals(10, new MenuService(state).availableCount());
    }

    @Test
    void aMissingOrUnreadableStateFileMeansEverythingIsAvailable() throws IOException {
        assertEquals(10, new MenuService(dir.resolve("absent.txt")).availableCount());
        Path folderInsteadOfFile = Files.createDirectory(dir.resolve("oops"));
        assertEquals(10, new MenuService(folderInsteadOfFile).availableCount());
    }

    @Test
    void pricesAreExactDecimals() {
        MenuService menu = new MenuService(dir.resolve("state.txt"));
        assertEquals("2.80", find(menu, "Iced Caramel Latte").getPrice().toPlainString());
    }
}
