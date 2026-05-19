package com.baitaplon.baitaplon;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class SellerController {
    @FXML private TextField txtProductName;
    @FXML private TextField txtPrice;
    @FXML private TextField txtTime;
    @FXML
    private TableColumn<Product,String> topBidColumn;
    @FXML private TableView<Product> sellerTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, String> colTime;
    @FXML
    private Label userName;
    @FXML
    private Label Wallet;
    @FXML
    private Button refreshButton;
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    public void initialize(){
        User loggedInUser = UserStorage.currentUser;
        if(loggedInUser !=null){
            userName.setText(loggedInUser.getUsername());
            userName.setTextFill(Color.GREEN);
            if(loggedInUser instanceof Seller){
                Seller seller=(Seller) loggedInUser;
                Wallet.setText(String.valueOf(seller.getMoney()));
            }
        }
        colName.setCellValueFactory(sanpham -> new SimpleStringProperty(sanpham.getValue().getName()));
        colPrice.setCellValueFactory(gia->new SimpleObjectProperty<>(gia.getValue().getPrice()));
        colTime.setCellValueFactory(time-> new SimpleStringProperty(time.getValue().getTimeDisplay()));
        topBidColumn.setCellValueFactory(cellData->{
            Bidder highestBidder = cellData.getValue().getHighestBidder();
            if (highestBidder != null){
                return new SimpleObjectProperty<>(highestBidder.getUsername());
            }
            else{
                return new SimpleObjectProperty<>("Chưa có");
            }
        });
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event-> {

            sellerTable.refresh();
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }
    @FXML
    protected void onSellButtonClick(ActionEvent event) {
        try {
            String name = txtProductName.getText();
            double price = Double.parseDouble(txtPrice.getText());
            int time = Integer.parseInt(txtTime.getText());

            User currentUser = UserStorage.currentUser;

            if (currentUser instanceof Seller) {
                Seller seller = (Seller) currentUser;
                seller.pushItem(name, price, time);

                System.out.println("Đăng bán thành công: " + name);

                txtProductName.clear();
                txtPrice.clear();
                txtTime.clear();
                refreshTable();
            }

        } catch (NumberFormatException e) {
            System.out.println("Lỗi: Vui lòng nhập số hợp lệ cho Giá và Thời gian!");
        }
    }
    private void refreshTable() {
        if (sellerTable != null) {
            productList.clear();
            productList.addAll(ProductStorage.getAllProducts().values());
            sellerTable.setItems(productList);
            sellerTable.refresh();
        }
    }
    @FXML
    protected void onRefreshButtonClick(ActionEvent event) {
        System.out.println("Bạn vừa bấm nút Làm mới danh sách");
        if (sellerTable != null) {
            User loggedInUser = UserStorage.currentUser;
            if (loggedInUser instanceof Seller) {
                Seller seller=(Seller) loggedInUser;
                Wallet.setText(String.valueOf(seller.getMoney()));
                Wallet.setTextFill(Color.GREEN);
            }
            sellerTable.refresh();
            System.out.println("Đã làm mới bảng dữ liệu");
        }
        else{
            System.out.println("bảng rỗng");
        }
    }
}
