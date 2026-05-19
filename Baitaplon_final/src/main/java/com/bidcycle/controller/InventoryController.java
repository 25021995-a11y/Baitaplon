package com.bidcycle.controller;

import com.bidcycle.model.Product;
import com.bidcycle.model.User;
import com.bidcycle.model.UserSession;
import com.bidcycle.util.ViewNavigator;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;

public class InventoryController {

    @FXML private TableView<Product>           inventoryTable;
    @FXML private TableColumn<Product, String> productColumn;
    @FXML private TableColumn<Product, Double> priceColumn;

    @FXML
    public void initialize() {
        productColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        priceColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrice()));
        priceColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("$%.2f", v));
            }
        });

        User user = UserSession.getCurrentUser();
        if (user != null) {
            inventoryTable.setItems(FXCollections.observableArrayList(user.getInventory()));
        }
    }

    @FXML
    protected void onBackToMain(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ViewNavigator.navigateTo(stage, ViewNavigator.BIDDER_HOME, "BidCycle - Sàn đấu giá");
        } catch (IOException e) { e.printStackTrace(); }
    }
}
