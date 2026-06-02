package com.bidcycle.controller;

import com.bidcycle.dao.ProductDAO;
import com.bidcycle.model.*;
import com.bidcycle.util.ViewNavigator;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;

/**
 * CONTROLLER: Màn hình đăng bán sản phẩm.
 * Mọi user đều có thể sử dụng màn hình này.
 */
public class SellerController {

    @FXML private TextField   txtProductName;
    @FXML private TextField   txtPrice;
    @FXML private TextField   txtTime;
    @FXML private TextArea    txtDescription;
    @FXML private TextField   txtImagePath;
    @FXML private ImageView   previewImage;
    @FXML private Label       lblImageStatus;
    @FXML private ComboBox<String> txtCategory;

    @FXML private TableView<Product>           sellerTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Double> colHighestPrice;
    @FXML private TableColumn<Product, String> colTime;
    @FXML private TableColumn<Product, String> topBidColumn;

    @FXML private Label userName;
    @FXML private Label walletLabel;
    @FXML private Label statusLabel;

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private String selectedImagePath = "";

    @FXML
    public void initialize() {
        User user = UserSession.getCurrentUser();
        if (user != null) {
            userName.setText(user.getUsername());
            userName.setTextFill(Color.valueOf("#c77dff"));
            refreshWallet(user);
        }

        if (txtCategory != null) {
            txtCategory.getItems().addAll(Product.CATEGORIES);
            txtCategory.getSelectionModel().selectFirst();
        }

        setupTableColumns();

        sellerTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Product p = sellerTable.getSelectionModel().getSelectedItem();
                if (p != null) openProductDetail(p);
            }
        });

        refreshTable();

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> sellerTable.refresh()));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void setupTableColumns() {
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        if (colCategory != null)
            colCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));

        colPrice.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getStartPrice()));
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("$%.2f", v));
            }
        });

        if (colHighestPrice != null) {
            colHighestPrice.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrice()));
            colHighestPrice.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(Double v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty || v == null) { setText(null); setStyle(""); return; }
                    setText(String.format("$%.2f", v));
                    setStyle("-fx-text-fill: #00e676; -fx-font-weight: bold;");
                }
            });
        }

        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTimeDisplay()));
        topBidColumn.setCellValueFactory(c -> {
            User hb = c.getValue().getHighestBidder();
            return new SimpleStringProperty(hb != null ? hb.getUsername() : "Chưa có");
        });
    }

    @FXML
    protected void onChooseImage(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn ảnh sản phẩm");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Ảnh", "*.png","*.jpg","*.jpeg","*.gif","*.bmp","*.webp")
        );
        File file = fc.showOpenDialog((Stage) ((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            if (txtImagePath != null) txtImagePath.setText(file.getName());
            if (previewImage != null) {
                try { previewImage.setImage(new Image(file.toURI().toString())); previewImage.setVisible(true); }
                catch (Exception ex) { previewImage.setVisible(false); }
            }
            if (lblImageStatus != null) {
                lblImageStatus.setText("✅ " + file.getName());
                lblImageStatus.setTextFill(Color.valueOf("#00e676"));
            }
        }
    }

    @FXML
    protected void onSellButtonClick(ActionEvent event) {
        User user = UserSession.getCurrentUser();
        if (user == null) { showStatus("⚠ Vui lòng đăng nhập!", Color.RED); return; }

        String name = txtProductName.getText().trim();
        if (name.isEmpty()) { showStatus("⚠ Nhập tên sản phẩm!", Color.ORANGE); return; }

        double price;
        int    minutes;
        try {
            price   = Double.parseDouble(txtPrice.getText().trim());
            minutes = Integer.parseInt(txtTime.getText().trim());
            if (price <= 0)   { showStatus("⚠ Giá phải lớn hơn 0!", Color.ORANGE);       return; }
            if (minutes <= 0) { showStatus("⚠ Thời gian phải lớn hơn 0!", Color.ORANGE); return; }
        } catch (NumberFormatException e) {
            showStatus("⚠ Giá và Thời gian phải là số!", Color.RED); return;
        }
        // Lưu sản phẩm vào database, dùng userId thay vì roleName
        boolean isSuccess = ProductDAO.insertProduct(name, price, minutes, user.getUserId());
        if (!isSuccess) {
            showStatus("⚠ Đăng bán thất bại! Kiểm tra kết nối database.", Color.RED);
            return;
        }

        showStatus("✅ Đăng bán thành công: \"" + name + "\" ($"
                   + String.format("%.2f", price) + " / " + minutes + " phút)",
                   Color.valueOf("#00e676"));
        clearForm();
        // Reload cả hai bảng từ database
        refreshTable();
    }

    @FXML protected void onNavHome(ActionEvent event)           { nav(event, ViewNavigator.BIDDER_HOME, "BidCycle — Trang chủ"); }
    @FXML protected void onRefreshButtonClick(ActionEvent event){ refreshWallet(UserSession.getCurrentUser()); refreshTable(); }
    @FXML protected void onNavAccount(ActionEvent event)        { nav(event, ViewNavigator.ACCOUNT, "BidCycle — Tài khoản"); }
    @FXML protected void onLogout(ActionEvent event)            { UserSession.clearSession(); nav(event, ViewNavigator.LOGIN, "BidCycle — Đăng nhập"); }

    private void openProductDetail(Product product) {
        try {
            FXMLLoader loader = ViewNavigator.createLoader(ViewNavigator.PRODUCT_DETAIL);
            Parent root = loader.load();
            ProductDetailController ctrl = loader.getController();
            ctrl.setProduct(product);
            Stage s = new Stage();
            s.initModality(Modality.APPLICATION_MODAL);
            s.setTitle("Chi tiết: " + product.getName());
            s.setScene(new Scene(root));
            s.setOnCloseRequest(e -> ctrl.dispose());
            s.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void refreshTable() {
        productList.setAll(ProductDAO.getAllProducts());
        sellerTable.setItems(productList);
        sellerTable.refresh();
    }

    private void clearForm() {
        txtProductName.clear(); txtPrice.clear(); txtTime.clear();
        if (txtDescription != null) txtDescription.clear();
        if (txtImagePath   != null) txtImagePath.clear();
        if (previewImage   != null) previewImage.setVisible(false);
        if (lblImageStatus != null) lblImageStatus.setText("");
        if (txtCategory    != null) txtCategory.getSelectionModel().selectFirst();
        selectedImagePath = "";
    }

    private void refreshWallet(User user) {
        if (user == null || walletLabel == null) return;
        walletLabel.setText(String.format("$%.2f  (khả dụng: $%.2f)", user.getMoney(), user.getVirMoney()));
        walletLabel.setTextFill(Color.valueOf("#00e5b0"));
    }

    private void nav(ActionEvent event, String fxml, String title) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ViewNavigator.navigateTo(stage, fxml, title);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showStatus(String msg, Color color) {
        if (statusLabel != null) { statusLabel.setText(msg); statusLabel.setTextFill(color); }
    }
}
