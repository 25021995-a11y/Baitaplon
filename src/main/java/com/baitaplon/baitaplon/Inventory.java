package com.baitaplon.baitaplon;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.IOException;
public class Inventory {
    @FXML
    private TableView<Product> inventoryTable;
    @FXML
    private Button backToMain;
    @FXML
    private TableColumn<Product, String> ProductColumn;

    @FXML
    private TableColumn<Product, Double> PriceColumn;
    @FXML
    //Mới chỉnh chỉ Bidder mới xem được inventory
    public void initialize(){
        User loggedInUser = UserStorage.currentUser;
        if(loggedInUser instanceof Bidder){
            Bidder bidder = (Bidder) loggedInUser;
            ObservableList<Product> inventoryData = FXCollections.observableArrayList(bidder.getInv());
            inventoryTable.setItems(inventoryData);
        }
        ProductColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        PriceColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrice()));
    }
    @FXML
    protected void backToMainButtonClick(ActionEvent event){
        try{
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
            Parent root = fxmlLoader.load();
            Scene auctionScene = new Scene(root);
            currentStage.setScene(auctionScene);
            currentStage.centerOnScreen();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
