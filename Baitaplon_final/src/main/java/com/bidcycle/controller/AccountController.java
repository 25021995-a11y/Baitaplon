package com.bidcycle.controller;

import com.bidcycle.dao.UserDAO;
import com.bidcycle.model.*;
import com.bidcycle.util.ViewNavigator;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AccountController {

    @FXML private Label lblUsername;
    @FXML private Label lblEmail;
    @FXML private Label lblGender;
    @FXML private Label lblRole;
    @FXML private Label lblCreatedDate;
    @FXML private Label lblStatus;
    @FXML private Label lblWallet;
    @FXML private Label lblVirWallet;
    @FXML private Canvas bidChartCanvas;
    @FXML private VBox chartContainer;
    @FXML private Label lblNoBidHistory;

    @FXML private VBox editPanel;
    @FXML private TextField editEmailField;
    @FXML private ComboBox<String> editGenderCombo;
    @FXML private PasswordField editNewPassword;
    @FXML private PasswordField editConfirmPassword;
    @FXML private Label editStatusLabel;

    private Timeline chartUpdateTimeline;

    @FXML
    public void initialize() {
        User user = UserSession.getCurrentUser();
        if (user == null) return;

        refreshDisplay(user);
        setupBidChart(user);

        chartUpdateTimeline = new Timeline(
            new KeyFrame(Duration.seconds(2), e -> setupBidChart(user))
        );
        chartUpdateTimeline.setCycleCount(Animation.INDEFINITE);
        chartUpdateTimeline.play();

        if (editGenderCombo != null)
            editGenderCombo.getItems().addAll("Nam", "Nữ", "Khác");
        if (editPanel != null) editPanel.setVisible(false);
    }

    private void refreshDisplay(User user) {
        lblUsername.setText(user.getUsername());
        lblEmail.setText(user.getEmail().isEmpty() ? "Chưa cập nhật" : user.getEmail());
        lblGender.setText(user.getGender());
        if (lblRole != null) lblRole.setText("Thành viên");
        lblCreatedDate.setText(user.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblStatus.setText(user.getIsActive() ? "✅ Đang hoạt động" : "🚫 Bị khóa");
        lblStatus.setTextFill(user.getIsActive() ? Color.valueOf("#00e676") : Color.RED);
        lblWallet.setText(String.format("$%.2f", user.getMoney()));
        if (lblVirWallet != null)
            lblVirWallet.setText("Khả dụng: $" + String.format("%.2f", user.getVirMoney()));
    }

    @FXML
    protected void onEditProfile(ActionEvent event) {
        User user = UserSession.getCurrentUser();
        if (user == null || editPanel == null) return;
        boolean isVisible = editPanel.isVisible();
        editPanel.setVisible(!isVisible);
        if (!isVisible) {
            editEmailField.setText(user.getEmail());
            String g = user.getGender();
            editGenderCombo.getSelectionModel().select(
                "Nam".equals(g) ? 0 : "Nữ".equals(g) ? 1 : 2);
            editNewPassword.clear();
            editConfirmPassword.clear();
            if (editStatusLabel != null) editStatusLabel.setText("");
        }
    }

    @FXML
    protected void onSaveProfile(ActionEvent event) {
        User user = UserSession.getCurrentUser();
        if (user == null) return;

        String newEmail   = editEmailField.getText().trim();
        String newGender  = editGenderCombo.getValue();
        String newPass    = editNewPassword.getText();
        String confirmPass = editConfirmPassword.getText();

        if (!newEmail.isEmpty() && !newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showEditStatus("❌ Email không hợp lệ!", Color.RED); return;
        }
        if (!newPass.isEmpty()) {
            if (!newPass.equals(confirmPass)) {
                showEditStatus("❌ Mật khẩu xác nhận không khớp!", Color.RED); return;
            }
            if (newPass.length() < 8 || !newPass.matches(".*[A-Z].*") || !newPass.matches(".*[a-z].*")
                    || !newPass.matches(".*[0-9].*") || !newPass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
                showEditStatus("❌ Mật khẩu yếu! Cần ≥8 ký tự, hoa, thường, số, ký tự đặc biệt.", Color.RED); return;
            }
        }

        boolean ok = UserDAO.updateUserInfo(user.getUsername(), newEmail, newGender,
                newPass.isEmpty() ? null : newPass);

        if (ok) {
            user.setEmail(newEmail);
            user.setGender(newGender);
            if (!newPass.isEmpty()) user.setPassword(newPass);
            refreshDisplay(user);
            showEditStatus("✅ Cập nhật thành công!", Color.valueOf("#00e676"));
            editNewPassword.clear();
            editConfirmPassword.clear();
        } else {
            showEditStatus("❌ Lỗi khi lưu. Vui lòng thử lại!", Color.RED);
        }
    }

    @FXML
    protected void onCancelEdit(ActionEvent event) {
        if (editPanel != null) editPanel.setVisible(false);
    }

    private void showEditStatus(String msg, Color color) {
        if (editStatusLabel != null) { editStatusLabel.setText(msg); editStatusLabel.setTextFill(color); }
    }

    // ── Biểu đồ ──────────────────────────────────────────────

    private void setupBidChart(User user) {
        ArrayList<BidRecord> history = user.getBidHistory();
        if (history == null || history.isEmpty()) {
            if (chartContainer != null) chartContainer.setVisible(false);
            if (lblNoBidHistory != null) lblNoBidHistory.setVisible(true);
            return;
        }
        if (chartContainer != null) chartContainer.setVisible(true);
        if (lblNoBidHistory != null) lblNoBidHistory.setVisible(false);
        drawBidChart(history);
    }

    private void drawBidChart(List<BidRecord> history) {
        if (bidChartCanvas == null) return;
        double W = bidChartCanvas.getWidth(), H = bidChartCanvas.getHeight();
        GraphicsContext gc = bidChartCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, W, H);
        gc.setFill(Color.valueOf("#160c2a")); gc.fillRoundRect(0, 0, W, H, 12, 12);

        int n = history.size();
        if (n == 0) return;
        double padL = 60, padR = 20, padT = 20, padB = 40;
        double chartW = W - padL - padR, chartH = H - padT - padB;

        double minP = history.stream().mapToDouble(BidRecord::getPrice).min().orElse(0);
        double maxP = history.stream().mapToDouble(BidRecord::getPrice).max().orElse(1);
        if (maxP == minP) { maxP = minP + 100; minP = Math.max(0, minP - 100); }

        gc.setStroke(Color.valueOf("#2d1857")); gc.setLineWidth(1);
        for (int i = 0; i <= 5; i++) {
            double y = padT + chartH - (chartH * i / 5.0);
            gc.strokeLine(padL, y, padL + chartW, y);
            gc.setFill(Color.valueOf("#5a3d7a")); gc.setFont(Font.font("Segoe UI", 10));
            gc.fillText(String.format("$%.0f", minP + (maxP - minP) * i / 5.0), 2, y + 4);
        }

        double[] xs = new double[n], ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = padL + (n == 1 ? chartW / 2 : chartW * i / (n - 1));
            ys[i] = padT + chartH - ((history.get(i).getPrice() - minP) / (maxP - minP)) * chartH;
            gc.setFill(Color.valueOf("#5a3d7a"));
            gc.fillText(String.valueOf(i + 1), xs[i] - 3, H - 6);
        }

        // Vẽ biểu đồ dạng đường gấp khúc (Step Chart)
        gc.setStroke(Color.valueOf("#c77dff")); gc.setLineWidth(2.5);
        gc.beginPath();
        for (int i = 0; i < n; i++) {
            if (i == 0) {
                gc.moveTo(xs[i], ys[i]);
            } else {
                // Tạo đường gấp khúc (Step): đi ngang từ x cũ đến x mới ở y cũ, sau đó đi dọc lên y mới
                gc.lineTo(xs[i], ys[i - 1]);
                gc.lineTo(xs[i], ys[i]);
            }
        }
        gc.stroke();

        // Vẽ vùng gradient phía dưới đường gấp khúc
        double[] stepXs = new double[2 * n + 2];
        double[] stepYs = new double[2 * n + 2];
        stepXs[0] = xs[0]; stepYs[0] = ys[0];
        int k = 1;
        for (int i = 1; i < n; i++) {
            stepXs[k] = xs[i]; stepYs[k] = ys[i-1]; k++;
            stepXs[k] = xs[i]; stepYs[k] = ys[i]; k++;
        }
        stepXs[k] = xs[n-1]; stepYs[k] = padT + chartH; k++;
        stepXs[k] = xs[0]; stepYs[k] = padT + chartH; k++;
        gc.setFill(Color.valueOf("#9d4edd33"));
        gc.fillPolygon(stepXs, stepYs, k);
        for (int i = 0; i < n; i++) {
            gc.setFill(Color.valueOf("#9d4edd55")); gc.fillOval(xs[i]-7, ys[i]-7, 14, 14);
            gc.setFill(Color.valueOf("#c77dff"));   gc.fillOval(xs[i]-4, ys[i]-4, 8, 8);
            if (i == n-1) {
                gc.setFill(Color.valueOf("#ffcc00")); gc.setFont(Font.font("Segoe UI Bold", 11));
                gc.fillText(String.format("$%.0f", history.get(i).getPrice()), xs[i]-20, ys[i]-10);
            }
        }
        gc.setStroke(Color.valueOf("#4a3060")); gc.setLineWidth(1.5);
        gc.strokeLine(padL, padT+chartH, padL+chartW, padT+chartH);
        gc.strokeLine(padL, padT, padL, padT+chartH);
    }

    // ── Điều hướng ────────────────────────────────────────────

    @FXML
    protected void onBackHome(ActionEvent event) {
        stopTimeline();
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ViewNavigator.navigateTo(stage, ViewNavigator.BIDDER_HOME, "BidCycle - Trang chủ");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    protected void onLogout(ActionEvent event) {
        stopTimeline();
        UserSession.clearSession();
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ViewNavigator.navigateTo(stage, ViewNavigator.LOGIN, "BidCycle - Đăng nhập");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void stopTimeline() {
        if (chartUpdateTimeline != null) chartUpdateTimeline.stop();
    }
}
