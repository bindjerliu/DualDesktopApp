package com.omel.controller;

import com.omel.OmelApp;
import com.omel.model.CartItem;
import com.omel.model.MenuItem;
import com.omel.model.Money;
import com.omel.model.Order;
import com.omel.service.MenuService;
import com.omel.service.OrderDatabase;
import com.omel.service.Validation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserController {
    @FXML private FlowPane menuGrid;
    @FXML private TextField searchField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> pickupCombo;
    @FXML private TextArea notesArea;
    @FXML private ListView<String> cartList;
    @FXML private Label totalLabel;
    @FXML private Label statusLabel;

    private final MenuService menuService = new MenuService();
    private final OrderDatabase orderDatabase = new OrderDatabase();
    private final ObservableList<CartItem> cart = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        pickupCombo.setItems(FXCollections.observableArrayList("ASAP - 10 min", "12:15", "12:30", "12:45", "13:00", "13:15", "13:30", "14:00", "15:00"));
        pickupCombo.getSelectionModel().selectFirst();
        renderMenu();
        searchField.textProperty().addListener((obs, old, val) -> renderMenu());
        refreshCart();
    }

    private void renderMenu() {
        menuGrid.getChildren().clear();
        for (MenuItem item : menuService.search(searchField.getText())) {
            VBox card = new VBox(10);
            card.getStyleClass().add("menu-card");
            card.setPadding(new Insets(18));
            card.setPrefWidth(250);

            Label category = new Label(item.getCategory().toUpperCase());
            category.getStyleClass().add("eyebrow");

            Label name = new Label(item.getName());
            name.getStyleClass().add("item-title");
            name.setWrapText(true);

            Label desc = new Label(item.getDescription());
            desc.getStyleClass().add("muted-text");
            desc.setWrapText(true);

            Label dietary = new Label(item.getDietary());
            dietary.getStyleClass().add("pill");

            HBox bottom = new HBox(10);
            bottom.getStyleClass().add("space-between");
            Label price = new Label(Money.format(item.getPrice()));
            price.getStyleClass().add("price");
            Button add = new Button(item.isAvailable() ? "+ Add" : "Unavailable");
            add.getStyleClass().add("primary-button");
            add.setDisable(!item.isAvailable());
            add.setOnAction(e -> addToCart(item));
            bottom.getChildren().addAll(price, add);

            card.getChildren().addAll(category, name, desc, dietary, bottom);
            menuGrid.getChildren().add(card);
        }
    }

    private void addToCart(MenuItem item) {
        Optional<CartItem> existing = cart.stream().filter(c -> c.getMenuItem().getName().equals(item.getName())).findFirst();
        if (existing.isPresent()) existing.get().setQuantity(existing.get().getQuantity() + 1);
        else cart.add(new CartItem(item, 1));
        statusLabel.setText(item.getName() + " added to cart.");
        refreshCart();
    }

    @FXML private void removeSelected() {
        int selected = cartList.getSelectionModel().getSelectedIndex();
        if (selected >= 0 && selected < cart.size()) {
            cart.remove(selected);
            refreshCart();
        }
    }

    @FXML private void clearCart() {
        cart.clear();
        refreshCart();
    }

    @FXML private void placeOrder() {
        if (cart.isEmpty()) { alert("Cart is empty", "Please add at least one item before placing the order."); return; }
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        if (!Validation.isValidName(name)) { alert("Check your name", "Please enter your full name (up to " + Validation.MAX_NAME_LENGTH + " characters)."); return; }
        if (!Validation.isValidEmail(email)) { alert("Check your email", "Please enter a valid email address, e.g. name@rit.edu."); return; }
        if (!Validation.isNotesLengthOk(notesArea.getText())) { alert("Notes too long", "Please keep notes under " + Validation.MAX_NOTES_LENGTH + " characters."); return; }

        String items = cart.stream().map(CartItem::toOrderText).collect(Collectors.joining(", "));
        BigDecimal total = cartTotal();
        Order order = orderDatabase.create(name, email, pickupCombo.getValue(), items, total, notesArea.getText());
        cart.clear();
        refreshCart();
        statusLabel.setText("Order " + order.getId() + " placed successfully. Pickup: " + order.getPickupTime());
        alert("Order placed", "Your order number is " + order.getId() + ". Total: " + Money.format(total));
    }

    private BigDecimal cartTotal() {
        return cart.stream().map(CartItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void refreshCart() {
        cartList.setItems(FXCollections.observableArrayList(cart.stream()
                .map(c -> c.toOrderText() + "  •  " + Money.format(c.getLineTotal()))
                .toList()));
        totalLabel.setText(Money.format(cartTotal()));
    }

    private void alert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML private void back() { OmelApp.showLaunch(); }
}
