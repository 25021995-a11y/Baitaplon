package com.bidcycle.controller;

import com.bidcycle.dao.ProductDAO;
import com.bidcycle.exception.AuthenticationException;
import com.bidcycle.exception.BidCycleException;
import com.bidcycle.model.*;
import com.bidcycle.util.ViewNavigator;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Comparator;

/**
 * CONTROLLER: Trang chủ — dùng chung cho mọi người dùng.
 * Mỗi user vừa có thể đấu giá, vừa có thể bán hàng.
 */
public class BidderHomeController {

    @FXML private CheckBox           chkAutoBid;
    @FXML private TableView<Product> auctionTable;
    @FXML private TextField          bidInput;
    @FXML private TableColumn<Product, String> productColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Double> highestPriceColumn;
    @FXML private TableColumn<Product, String> timeColumn;
    @FXML private TableColumn<Product, String> topBidColumn;
    @FXML private TextField          txtStepPrice;
    @FXML private Label              statusLabel;
    @FXML private TextField          searchField;
    @FXML private Label              userName;
    @FXML private Label              walletLabel;
    @FXML private ComboBox<String>   categoryFilter;
    @FXML private ComboBox<String>   sortComboBox;
    @FXML private Label              headerTitle;
    @FXML private Label              headerSubtitle;

    private final ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private FilteredList<Product> filteredProducts;

    @FXML
    public void initialize() {
        User user = UserSession.getCurrentUser();
        if (user != null) {
            userName.setText(user.getUsername());
            userName.setTextFill(Color.valueOf("#c77dff"));
            updateWallet();
        }
        setupTable();
        setupFilters();
        loadProducts();
        startClock();
    }

    private void setupTable() {
        productColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        categoryColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));

        priceColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getStartPrice()));
        priceColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("$%.2f", v));
            }
        });

        highestPriceColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrice()));
        highestPriceColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(String.format("$%.2f", v));
                setStyle("-fx-text-fill: #00e676; -fx-font-weight: bold;");
            }
        });

        timeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTimeDisplay()));
        timeColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                setStyle(v.equals("Đã kết thúc") ? "-fx-text-fill: #ff5555;" : "-fx-text-fill: #ffcc00;");
            }
        });

        topBidColumn.setCellValueFactory(c -> {
            User hb = c.getValue().getHighestBidder();
            return new SimpleStringProperty(hb != null ? hb.getUsername() : "Chưa có");
        });

        auctionTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Product p = auctionTable.getSelectionModel().getSelectedItem();
                if (p != null) { p.incrementSearchCount(); openProductDetail(p); }
            }
        });
    }

    private void setupFilters() {
        categoryFilter.getItems().add("Tất cả");
        categoryFilter.getItems().addAll(Product.CATEGORIES);
        categoryFilter.getSelectionModel().selectFirst();

        sortComboBox.getItems().addAll("Mặc định", "Tên A→Z", "Tên Z→A", "Giá thấp→cao", "Giá cao→thấp");
        sortComboBox.getSelectionModel().selectFirst();

        searchField.textProperty().addListener((o, ov, nv)    -> applyFilters());
        categoryFilter.valueProperty().addListener((o, ov, nv) -> applyFilters());
        sortComboBox.valueProperty().addListener((o, ov, nv)   -> applyFilters());
    }

    private void loadProducts() {
        allProducts.setAll(ProductDAO.getAllProducts());
        filteredProducts = new FilteredList<>(allProducts, p -> true);
        applyFilters();
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            allProducts.forEach(Product::checkAndProcessEnd);
            auctionTable.refresh();
            updateWallet();
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void applyFilters() {
        String kw    = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String cat   = categoryFilter.getValue();
        boolean allCat = cat == null || cat.equals("Tất cả");

        filteredProducts.setPredicate(p -> {
            boolean matchKw  = kw.isEmpty()
                || p.getName().toLowerCase().contains(kw)
                || p.getDescription().toLowerCase().contains(kw)
                || (p.getOwner() != null && p.getOwner().getUsername().toLowerCase().contains(kw));
            boolean matchCat = allCat || p.getCategory().equals(cat);
            return matchKw && matchCat;
        });

        String sort = sortComboBox.getValue();
        if (sort == null) sort = "Mặc định";
        ObservableList<Product> list = FXCollections.observableArrayList(filteredProducts);
        switch (sort) {
            case "Tên A→Z"      -> list.sort(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
            case "Tên Z→A"      -> list.sort(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER).reversed());
            case "Giá thấp→cao" -> list.sort(Comparator.comparingDouble(Product::getPrice));
            case "Giá cao→thấp" -> list.sort(Comparator.comparingDouble(Product::getPrice).reversed());
        }
        auctionTable.setItems(list);
    }

    // ── Đặt giá ──────────────────────────────────────────────

    @FXML
    protected void onBidButtonClick(ActionEvent event) {
        User user = UserSession.getCurrentUser();
        if (user == null) {
            showStatus(AuthenticationException.unauthorized().getMessage(), Color.RED);
            return;
        }

        Product selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected == null)       { showStatus("Vui lòng chọn một sản phẩm!", Color.ORANGE); return; }
        if (selected.getFinished()) { showStatus("Phiên đấu giá đã kết thúc!", Color.RED);     return; }

        // Kiểm tra không đấu giá sản phẩm của chính mình
        if (selected.getOwner() != null && selected.getOwner().getUserId() == user.getUserId()) {
            showStatus("Bạn không thể đấu giá sản phẩm của chính mình!", Color.ORANGE); return;
        }

        String bidText = bidInput.getText();
        if (bidText == null || bidText.isBlank()) { showStatus("Chưa nhập số tiền!", Color.RED); return; }

        try {
            double bidPrice = Double.parseDouble(bidText.replace("$", "").trim());
            boolean autoBid = chkAutoBid.isSelected();
            double stepPrice = 0.0;

            if (autoBid) {
                String stepText = txtStepPrice.getText();
                if (stepText == null || stepText.isBlank()) {
                    showStatus("Nhập bước giá cho Auto-Bid!", Color.RED); return;
                }
                stepPrice = Double.parseDouble(stepText.trim());
            }

            Product.BidResult result = selected.processBid(user, bidPrice, autoBid, stepPrice);
            if (result.isSuccess) {
                // Lấy ID sản phẩm, ID người đặt, và Giá hiện tại vừa được processBid cập nhật
                ProductDAO.updateHighestBid(selected.getProductId(), user.getUserId(), selected.getPrice());
            }
            showStatus(result.message, result.isSuccess ? Color.valueOf("#00e676") : Color.RED);
            bidInput.clear();
            txtStepPrice.clear();
            updateWallet();
            applyFilters();

        } catch (BidCycleException ex) {
            showStatus(ex.getMessage(), Color.RED);
        } catch (NumberFormatException ex) {
            showStatus("Số tiền không hợp lệ!", Color.RED);
        }
    }

    // ── Dashboard ─────────────────────────────────────────────

    @FXML
    protected void onNavDashboard(ActionEvent event) {
        nav(event, ViewNavigator.DASHBOARD, "BidCycle — My Dashboard");
    }

    // ── Điều hướng ────────────────────────────────────────────

    @FXML protected void onNavHome(ActionEvent e)            { onRefreshButtonClick(e); }
    @FXML protected void onNavAccount(ActionEvent e)         { nav(e, ViewNavigator.ACCOUNT,      "BidCycle - Tài khoản"); }
    @FXML protected void onPayButtonClick(ActionEvent e)     { nav(e, ViewNavigator.PAYMENT,      "BidCycle - Nạp tiền"); }
    @FXML protected void onInventoryButtonClick(ActionEvent e){ nav(e, ViewNavigator.INVENTORY,   "BidCycle - Kho hàng"); }
    @FXML protected void onNavSell(ActionEvent e)            { nav(e, ViewNavigator.SELLER_HOME,  "BidCycle - Đăng bán"); }

    @FXML
    protected void onRefreshButtonClick(ActionEvent event) {
        resetHeader();
        allProducts.setAll(ProductDAO.getAllProducts());
        filteredProducts = new FilteredList<>(allProducts, p -> true);
        applyFilters();
        updateWallet();
    }

    @FXML
    protected void onLogout(ActionEvent event) {
        UserSession.clearSession();
        nav(event, ViewNavigator.LOGIN, "BidCycle - Đăng nhập");
    }

    // ── Helpers ───────────────────────────────────────────────

    private void openProductDetail(Product product) {
        try {
            FXMLLoader loader = ViewNavigator.createLoader(ViewNavigator.PRODUCT_DETAIL);
            Parent root = loader.load();
            ProductDetailController ctrl = loader.getController();
            ctrl.setProduct(product);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chi tiết: " + product.getName());
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(e -> ctrl.dispose());
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
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

    private void updateWallet() {
        User user = UserSession.getCurrentUser();
        if (walletLabel == null || user == null) return;
        walletLabel.setText(String.format("$%.2f  (khả dụng: $%.2f)", user.getMoney(), user.getVirMoney()));
    }

    private void resetHeader() {
        if (headerTitle != null) headerTitle.setText("Trang chủ — Sàn đấu giá BidCycle");
        if (headerSubtitle != null) headerSubtitle.setText("Tất cả phiên đấu giá đang diễn ra");
    }
}
