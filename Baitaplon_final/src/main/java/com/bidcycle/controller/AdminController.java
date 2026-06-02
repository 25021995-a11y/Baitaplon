package com.bidcycle.controller;

import com.bidcycle.dao.AdminDAO;
import com.bidcycle.model.Admin;
import com.bidcycle.model.User;
import com.bidcycle.model.UserSession;
import com.bidcycle.util.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

/**
 * CONTROLLER: Admin Management Dashboard
 * Quản lý: tài khoản, vật phẩm, thống kê.
 */
public class AdminController {

    // ── Tabs ───────────────────────────────────────────────────
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabUsers;
    @FXML private Tab tabItems;
    @FXML private Tab tabDisputes;
    @FXML private Tab tabConfig;

    // ── Dashboard ──────────────────────────────────────────────
    @FXML private Label lblTotalUsers;
    @FXML private Label lblActiveUsers;
    @FXML private Label lblBannedUsers;
    @FXML private Label lblTotalProducts;
    @FXML private Label lblOngoingAuctions;
    @FXML private Label lblFinishedAuctions;
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblFeeRevenue;

    // ── Users Management ───────────────────────────────────────
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, Boolean> colStatus;
    @FXML private TableColumn<User, Double> colBalance;
    @FXML private Button btnApproveUser;
    @FXML private Button btnBanUser;
    @FXML private Button btnSetAdmin;
    @FXML private TextField txtBanReason;

    // ── Items Management ───────────────────────────────────────
    @FXML private TableView<Map<String, Object>> itemsTable;
    @FXML private TableColumn<Map<String, Object>, Integer> colItemId;
    @FXML private TableColumn<Map<String, Object>, String> colItemName;
    @FXML private TableColumn<Map<String, Object>, String> colOwner;
    @FXML private TableColumn<Map<String, Object>, Double> colPrice;
    @FXML private Button btnApproveItem;
    @FXML private Button btnRejectItem;
    @FXML private Button btnStopAuction;

    // ── Disputes ───────────────────────────────────────────────
    @FXML private TableView<Map<String, Object>> disputesTable;
    @FXML private TableColumn<Map<String, Object>, String> colProduct;
    @FXML private TableColumn<Map<String, Object>, String> colComplainant;
    @FXML private TableColumn<Map<String, Object>, String> colRespondent;
    @FXML private TableColumn<Map<String, Object>, String> colReason;
    @FXML private TableColumn<Map<String, Object>, String> colDisputeStatus;
    @FXML private Button btnResolveDispute;
    @FXML private Button btnRefundPayment;
    @FXML private TextArea txtDisputeResolution;

    // ── Config ─────────────────────────────────────────────────
    @FXML private Spinner<Double> spinTransactionFee;
    @FXML private Button btnSaveFee;
    @FXML private TextArea txtReport;
    @FXML private Button btnGenerateReport;

    // ── Navigation ─────────────────────────────────────────────
    @FXML private Label lblAdminName;

    @FXML
    public void initialize() {
        // Kiểm tra quyền admin
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null || !currentUser.getRoleName().equals("Admin")) {
            showAlert("Lỗi", "Bạn không có quyền truy cập trang này!");
            return;
        }

        lblAdminName.setText("Admin: " + currentUser.getUsername());

        // Khởi tạo các tab
        initDashboard();
        initUsersTable();
        initItemsTable();
        initConfigTab();

        // Load dữ liệu
        refreshDashboard();
        refreshUsersList();
        refreshItemsList();
    }

    // ════════════════════════════════════════════════════════════════════
    // DASHBOARD
    // ════════════════════════════════════════════════════════════════════

    private void initDashboard() {
        // Dashboard hiển thị thống kê tổng quát
    }

    private void refreshDashboard() {
        Map<String, Object> report = AdminDAO.generateReport();

        if (lblTotalUsers != null) lblTotalUsers.setText(String.valueOf(report.get("totalUsers")));
        if (lblActiveUsers != null) lblActiveUsers.setText(String.valueOf(report.get("activeUsers")));
        if (lblBannedUsers != null) lblBannedUsers.setText(String.valueOf(report.get("bannedUsers")));
        if (lblTotalProducts != null) lblTotalProducts.setText(String.valueOf(report.get("totalProducts")));
        if (lblOngoingAuctions != null) lblOngoingAuctions.setText(String.valueOf(report.get("ongoingProducts")));
        if (lblFinishedAuctions != null) lblFinishedAuctions.setText(String.valueOf(report.get("finishedProducts")));

        double totalRev = 0.0;
        Object trv = report.get("totalTransactionValue");
        if (trv instanceof Number) totalRev = ((Number) trv).doubleValue();
        if (lblTotalRevenue != null) lblTotalRevenue.setText(String.format("$%.2f", totalRev));

        double feeRev = 0.0;
        Object fr = report.get("feeRevenue");
        if (fr instanceof Number) feeRev = ((Number) fr).doubleValue();
        if (lblFeeRevenue != null) lblFeeRevenue.setText(String.format("$%.2f", feeRev));
    }

    // ════════════════════════════════════════════════════════════════════
    // USERS MANAGEMENT
    // ════════════════════════════════════════════════════════════════════

    private void initUsersTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("isActive"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("money"));

        // Format status: Active/Inactive
        colStatus.setCellFactory(col -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item ? "✓ Hoạt động" : "✗ Bị cấm");
                setTextFill(item != null && item ? Color.GREEN : Color.RED);
            }
        });

        // Format balance
        colBalance.setCellFactory(col -> new TableCell<User, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("$%.2f", item));
            }
        });
    }

    private void refreshUsersList() {
        List<User> users = AdminDAO.getAllUsers();
        usersTable.getItems().clear();
        usersTable.getItems().addAll(users);
    }

    @FXML
    private void onApproveUser(ActionEvent event) {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Chọn người dùng để duyệt!");
            return;
        }

        if (AdminDAO.approveAccount(selected.getUserId())) {
            showAlert("Thành công", "Tài khoản đã được kích hoạt!");
            refreshUsersList();
            refreshDashboard();
        } else {
            showAlert("Lỗi", "Không thể kích hoạt tài khoản!");
        }
    }

    @FXML
    private void onBanUser(ActionEvent event) {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Chọn người dùng để cấm!");
            return;
        }

        String reason = txtBanReason.getText().trim();
        if (reason.isEmpty()) {
            reason = "Không có lý do";
        }

        if (AdminDAO.banUser(selected.getUserId(), reason)) {
            showAlert("Thành công", "Tài khoản đã bị cấm!");
            txtBanReason.clear();
            refreshUsersList();
            refreshDashboard();
        } else {
            showAlert("Lỗi", "Không thể cấm tài khoản!");
        }
    }

    @FXML
    private void onSetAdmin(ActionEvent event) {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Chọn người dùng để cấp quyền Admin!");
            return;
        }

        if (selected.getRoleName().equals("Admin")) {
            showAlert("Thông báo", "Người dùng này đã là Admin!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Cấp quyền Quản trị viên");
        confirm.setContentText("Bạn có chắc chắn muốn cấp quyền Admin cho '" + selected.getUsername() + "'?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (AdminDAO.updateUserRole(selected.getUserId(), "ADMIN")) {
                showAlert("Thành công", "Đã cấp quyền Admin cho " + selected.getUsername());
                refreshUsersList();
            } else {
                showAlert("Lỗi", "Không thể cập nhật quyền hạn!");
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // ITEMS MANAGEMENT
    // ════════════════════════════════════════════════════════════════════

    private void initItemsTable() {
        colItemId.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>((Integer) cellData.getValue().get("productId")));
        colItemName.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>((String) cellData.getValue().get("name")));
        colOwner.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>((String) cellData.getValue().get("ownerName")));
        colPrice.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>((Double) cellData.getValue().get("startPrice")));

        colPrice.setCellFactory(col -> new TableCell<Map<String, Object>, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("$%.2f", item));
            }
        });
    }

    private void refreshItemsList() {
        List<Map<String, Object>> items = AdminDAO.getPendingItems();
        itemsTable.getItems().clear();
        itemsTable.getItems().addAll(items);
    }

    @FXML
    private void onApproveItem(ActionEvent event) {
        Map<String, Object> selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Chọn vật phẩm để duyệt!");
            return;
        }

        int productId = (Integer) selected.get("productId");
        if (AdminDAO.approveItem(productId)) {
            showAlert("Thành công", "Vật phẩm đã được duyệt!");
            refreshItemsList();
        } else {
            showAlert("Lỗi", "Không thể duyệt vật phẩm!");
        }
    }

    @FXML
    private void onRejectItem(ActionEvent event) {
        Map<String, Object> selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Chọn vật phẩm để từ chối!");
            return;
        }

        int productId = (Integer) selected.get("productId");
        if (AdminDAO.rejectItem(productId, "Từ chối bởi admin")) {
            showAlert("Thành công", "Vật phẩm đã bị từ chối!");
            refreshItemsList();
        } else {
            showAlert("Lỗi", "Không thể từ chối vật phẩm!");
        }
    }

    @FXML
    private void onStopAuction(ActionEvent event) {
        Map<String, Object> selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Chọn phiên đấu giá để dừng!");
            return;
        }

        int productId = (Integer) selected.get("productId");
        if (AdminDAO.forceStopAuction(productId)) {
            showAlert("Thành công", "Phiên đấu giá đã bị dừng!");
            refreshItemsList();
        } else {
            showAlert("Lỗi", "Không thể dừng một phiên đấu giá!");
        }
    }
    // ════════════════════════════════════════════════════════════════════
    // CONFIG
    // ════════════════════════════════════════════════════════════════════

    private void initConfigTab() {
        spinTransactionFee.setValueFactory(
            new javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100,
                AdminDAO.getTransactionFee() * 100, 0.1)
        );
    }

    @FXML
    private void onSaveFee(ActionEvent event) {
        double feePercent = spinTransactionFee.getValue();
        if (AdminDAO.setTransactionFee(feePercent / 100)) {
            showAlert("Thành công", "Phí giao dịch đã được cập nhật: " + feePercent + "%");
            refreshDashboard();
        } else {
            showAlert("Lỗi", "Không thể cập nhật phí giao dịch!");
        }
    }

    @FXML
    private void onGenerateReport(ActionEvent event) {
        Map<String, Object> report = AdminDAO.generateReport();
        StringBuilder sb = new StringBuilder();

        sb.append("╔════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    BÁO CÁO THỐNG KÊ HỆ THỐNG                        ║\n");
        sb.append("╚════════════════════════════════════════════════════════════════════╝\n\n");

        sb.append("📊 THỐNG KÊ NGƯỜI DÙNG\n");
        sb.append("├─ Tổng người dùng: ").append(report.get("totalUsers")).append("\n");
        sb.append("├─ Người dùng hoạt động: ").append(report.get("activeUsers")).append("\n");
        sb.append("└─ Người dùng bị cấm: ").append(report.get("bannedUsers")).append("\n\n");

        sb.append("🏪 THỐNG KÊ VẬT PHẨM\n");
        sb.append("├─ Tổng vật phẩm: ").append(report.get("totalProducts")).append("\n");
        sb.append("├─ Vật phẩm đang đấu giá: ").append(report.get("ongoingProducts")).append("\n");
        sb.append("└─ Vật phẩm đã kết thúc: ").append(report.get("finishedProducts")).append("\n\n");

        sb.append("💰 THỐNG KÊ TÀI CHÍ\n");
        sb.append("├─ Tổng giá trị giao dịch: $").append(String.format("%.2f", report.get("totalTransactionValue"))).append("\n");
        sb.append("├─ Tổng tiền nạp: $").append(String.format("%.2f", report.get("totalDeposits"))).append("\n");
        sb.append("└─ Doanh thu phí: $").append(String.format("%.2f", report.get("feeRevenue"))).append("\n\n");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topSellers = (List<Map<String, Object>>) report.get("topSellers");
        sb.append("🏆 TOP 5 NGƯỜI BÁN\n");
        if (topSellers != null) {
            for (int i = 0; i < Math.min(5, topSellers.size()); i++) {
                Map<String, Object> seller = topSellers.get(i);
                sb.append(String.format("%d. %s: %d sản phẩm, $%.2f revenue\n",
                    i+1, seller.get("username"), seller.get("soldCount"), seller.get("totalRevenue")));
            }
        }
        sb.append("\n");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topBidders = (List<Map<String, Object>>) report.get("topBidders");
        sb.append("🎯 TOP 5 NGƯỜI MUA\n");
        if (topBidders != null) {
            for (int i = 0; i < Math.min(5, topBidders.size()); i++) {
                Map<String, Object> bidder = topBidders.get(i);
                sb.append(String.format("%d. %s: %d sản phẩm, $%.2f chi tiêu\n",
                    i+1, bidder.get("username"), bidder.get("wonCount"), bidder.get("totalSpent")));
            }
        }

        txtReport.setText(sb.toString());
    }

    // ════════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ════════════════════════════════════════════════════════════════════

    @FXML
    protected void onLogout(ActionEvent event) {
        UserSession.clearSession();
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ViewNavigator.navigateTo(stage, ViewNavigator.LOGIN, "BidCycle - Đăng nhập");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    protected void onRefresh(ActionEvent event) {
        refreshDashboard();
        refreshUsersList();
        refreshItemsList();
        showAlert("Thông báo", "Đã làm mới tất cả dữ liệu!");
    }

    // ════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ════════════════════════════════════════════════════════════════════

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

