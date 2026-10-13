package com.omel.controller;

import com.omel.OmelApp;
import com.omel.model.MenuItem;
import com.omel.model.Money;
import com.omel.model.Order;
import com.omel.model.OrderStatus;
import com.omel.service.MenuService;
import com.omel.service.OrderDatabase;
import com.omel.service.OrderStats;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.util.List;

public class AdminController {
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> idColumn;
    @FXML private TableColumn<Order, String> timeColumn;
    @FXML private TableColumn<Order, String> customerColumn;
    @FXML private TableColumn<Order, String> pickupColumn;
    @FXML private TableColumn<Order, String> itemsColumn;
    @FXML private TableColumn<Order, BigDecimal> totalColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private Label revenueLabel;
    @FXML private Label activeOrdersLabel;
    @FXML private Label completedOrdersLabel;
    @FXML private Label inventoryLabel;
    @FXML private ListView<String> inventoryList;
    @FXML private TextArea detailsArea;

    private final OrderDatabase db = new OrderDatabase();
    private final MenuService menuService = new MenuService();

    @FXML public void initialize() {
        idColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        timeColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTimestamp()));
        customerColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomerName()));
        pickupColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPickupTime()));
        itemsColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getItems()));
        totalColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getTotal()));
        totalColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Money.format(item));
            }
        });
        statusColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> showDetails(val));
        refresh();
    }

    @FXML private void refresh() {
        List<Order> orders = db.findAll();
        ordersTable.setItems(FXCollections.observableArrayList(orders));
        revenueLabel.setText(Money.format(OrderStats.revenue(orders)));
        activeOrdersLabel.setText(String.valueOf(OrderStats.activeCount(orders)));
        completedOrdersLabel.setText(String.valueOf(OrderStats.completedCount(orders)));
        refreshInventory();
    }

    private void refreshInventory() {
        inventoryLabel.setText(menuService.availableCount() + "/" + menuService.getAllItems().size());
        inventoryList.setItems(FXCollections.observableArrayList(menuService.getAllItems().stream()
                .map(i -> (i.isAvailable() ? "✓ " : "× ") + i.getName() + " — " + i.getCategory()).toList()));
    }

    private void showDetails(Order o) {
        if (o == null) { detailsArea.clear(); return; }
        detailsArea.setText("Order: " + o.getId() + "\nCustomer: " + o.getCustomerName() + "\nEmail: " + o.getCustomerEmail()
                + "\nPickup: " + o.getPickupTime() + "\nStatus: " + o.getStatus() + "\nTotal: " + Money.format(o.getTotal())
                + "\n\nItems:\n" + o.getItems().replace(", ", "\n") + "\n\nNotes: " + o.getNotes());
    }

    private void updateSelected(OrderStatus status) {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!db.updateStatus(selected.getId(), status.name())) {
            new Alert(Alert.AlertType.WARNING, "That order no longer exists in the database.").showAndWait();
        }
        refresh();
    }

    @FXML private void markPreparing() { updateSelected(OrderStatus.PREPARING); }
    @FXML private void markReady() { updateSelected(OrderStatus.READY); }
    @FXML private void markCompleted() { updateSelected(OrderStatus.COMPLETED); }
    @FXML private void cancelOrder() { updateSelected(OrderStatus.CANCELLED); }

    @FXML private void toggleAvailability() {
        int index = inventoryList.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            MenuItem item = menuService.getAllItems().get(index);
            menuService.setAvailable(item, !item.isAvailable());
            refreshInventory();
            inventoryList.getSelectionModel().select(index);
        }
    }

    @FXML private void logout() { OmelApp.showLaunch(); }
}
