package com.bidcycle.controller;

import com.bidcycle.dao.UserDAO;
import com.bidcycle.model.User;
import com.bidcycle.model.UserSession;
import com.bidcycle.util.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CONTROLLER: Màn hình nạp tiền.
 * - Gợi ý 5 mức: $5, $10, $100, $200, $1000
 * - Cho phép nhập tay bất kỳ số >= $5
 * - Hiện dialog xác nhận trước khi thực hiện
 */
public class PaymentController {

    @FXML private Label     remainMoneyLabel;
    @FXML private Label     virMoneyLabel;
    @FXML private TextField customAmountField;
    @FXML private Label     inputErrorLabel;
    @FXML private Label     resultLabel;

    // Các nút gợi ý — để highlight nút đang được chọn
    @FXML private Button btn5;
    @FXML private Button btn10;
    @FXML private Button btn100;
    @FXML private Button btn200;
    @FXML private Button btn1000;

    private Button selectedQuickBtn = null;  // nút gợi ý đang được chọn

    @FXML
    public void initialize() {
        refreshDisplay();

        // Khi người dùng gõ vào ô tự nhập → bỏ chọn nút gợi ý
        customAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                clearQuickSelection();
            }
            if (inputErrorLabel != null) inputErrorLabel.setText("");
            if (resultLabel != null) resultLabel.setText("");
        });
    }

    // ── Cập nhật hiển thị số dư ──────────────────────────────

    private void refreshDisplay() {
        User user = UserSession.getCurrentUser();
        if (user == null) return;

        remainMoneyLabel.setText(String.format("$%.2f", user.getMoney()));

        if (virMoneyLabel != null) {
            virMoneyLabel.setText("Khả dụng đặt giá: $" + String.format("%.2f", user.getVirMoney()));
        }
    }

    // ── Các nút gợi ý nhanh ──────────────────────────────────

    @FXML protected void onQuickSelect5(ActionEvent e)    { selectQuick(btn5,    5); }
    @FXML protected void onQuickSelect10(ActionEvent e)   { selectQuick(btn10,   10); }
    @FXML protected void onQuickSelect100(ActionEvent e)  { selectQuick(btn100,  100); }
    @FXML protected void onQuickSelect200(ActionEvent e)  { selectQuick(btn200,  200); }
    @FXML protected void onQuickSelect1000(ActionEvent e) { selectQuick(btn1000, 1000); }

    /**
     * Bấm vào nút gợi ý: điền sẵn vào ô nhập và highlight nút đó.
     */
    private void selectQuick(Button btn, double amount) {
        // Highlight nút được chọn, bỏ highlight các nút khác
        clearQuickSelection();
        btn.setStyle(btn.getStyle()
            + " -fx-background-color: #7b2ff7; -fx-text-fill: white; -fx-border-color: #c77dff; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
        selectedQuickBtn = btn;

        // Điền vào ô nhập (nguyên số không có .0 nếu là số nguyên)
        String display = amount == (long) amount
                ? String.valueOf((long) amount)
                : String.valueOf(amount);
        customAmountField.setText(display);

        if (inputErrorLabel != null) inputErrorLabel.setText("");
        if (resultLabel != null) resultLabel.setText("");
    }

    private void clearQuickSelection() {
        // Reset style về mặc định cho tất cả nút gợi ý
        for (Button b : new Button[]{btn5, btn10, btn100, btn200, btn1000}) {
            if (b != null) {
                b.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
            }
        }
        selectedQuickBtn = null;
    }

    // ── Nút "Nạp tiền" chính ─────────────────────────────────

    @FXML
    protected void onDepositClick(ActionEvent event) {
        if (inputErrorLabel != null) inputErrorLabel.setText("");
        if (resultLabel != null)     resultLabel.setText("");

        // Đọc số tiền từ ô nhập
        String raw = customAmountField.getText();
        if (raw == null || raw.isBlank()) {
            showInputError("Vui lòng chọn gợi ý hoặc nhập số tiền muốn nạp!");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(raw.replace("$", "").replace(",", ".").trim());
        } catch (NumberFormatException e) {
            showInputError("Số tiền không hợp lệ! Vui lòng nhập số (ví dụ: 50, 200.5)");
            return;
        }

        if (amount < 5) {
            showInputError("Số tiền nạp tối thiểu là $5!");
            return;
        }

        // Hiện dialog xác nhận
        showConfirmDialog(amount);
    }

    // ── Dialog xác nhận (styled) ──────────────────────────────

    private void showConfirmDialog(double amount) {
        String amountStr = amount == (long) amount
                ? "$" + (long) amount
                : String.format("$%.2f", amount);

        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);

        // ── Root backdrop ──
        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: #00000088;");
        backdrop.setMinWidth(480);
        backdrop.setMinHeight(340);

        // ── Card ──
        VBox card = new VBox(22);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 40, 32, 40));
        card.setMaxWidth(400);
        card.setStyle(
            "-fx-background-color: #1a0d32;" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: #5a2d9a;" +
            "-fx-border-radius: 18;" +
            "-fx-border-width: 1.5;"
        );
        DropShadow glow = new DropShadow();
        glow.setColor(Color.valueOf("#7b2cbf88"));
        glow.setRadius(30);
        glow.setSpread(0.15);
        card.setEffect(glow);

        // Icon + title
        Label icon = new Label("💰");
        icon.setFont(Font.font(36));

        Label title = new Label("Xác nhận nạp tiền");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        title.setTextFill(Color.valueOf("#c77dff"));

        // Divider
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setPrefWidth(260);
        divider.setStyle("-fx-background-color: #2d1857;");

        // Amount display
        VBox amountBox = new VBox(4);
        amountBox.setAlignment(Pos.CENTER);
        amountBox.setPadding(new Insets(6, 20, 6, 20));
        amountBox.setStyle(
            "-fx-background-color: #0d0818;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #3d1d6e;" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;"
        );
        Label amountCaption = new Label("Số tiền sẽ nạp");
        amountCaption.setFont(Font.font("Segoe UI", 11));
        amountCaption.setTextFill(Color.valueOf("#6a4a8a"));
        Label amountLabel = new Label(amountStr);
        amountLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        amountLabel.setTextFill(Color.valueOf("#00e5b0"));
        DropShadow amtGlow = new DropShadow();
        amtGlow.setColor(Color.valueOf("#00e5b066")); amtGlow.setRadius(10);
        amountLabel.setEffect(amtGlow);
        amountBox.getChildren().addAll(amountCaption, amountLabel);

        Label question = new Label("Bạn có chắc chắn muốn thực hiện\ngiao dịch này không?");
        question.setFont(Font.font("Segoe UI", 13));
        question.setTextFill(Color.valueOf("#b0a0d0"));
        question.setTextAlignment(TextAlignment.CENTER);
        question.setWrapText(true);

        // Buttons row
        HBox buttons = new HBox(14);
        buttons.setAlignment(Pos.CENTER);

        AtomicBoolean confirmed = new AtomicBoolean(false);

        Button cancelBtn = new Button("✕  Hủy bỏ");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        cancelBtn.setStyle(
            "-fx-background-color: #1e1035;" +
            "-fx-text-fill: #9d4edd;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #3d1d6e;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        );
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(
            "-fx-background-color: #2d1857;" +
            "-fx-text-fill: #c77dff;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #7b2cbf;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        ));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(
            "-fx-background-color: #1e1035;" +
            "-fx-text-fill: #9d4edd;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #3d1d6e;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        ));
        cancelBtn.setOnAction(e -> dialog.close());

        Button confirmBtn = new Button("✅  Chắc chắn");
        confirmBtn.setPrefWidth(130);
        confirmBtn.setPrefHeight(40);
        confirmBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        confirmBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #6a1fb5, #9d4edd);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        DropShadow btnGlow = new DropShadow();
        btnGlow.setColor(Color.valueOf("#6a0dad66")); btnGlow.setRadius(10);
        confirmBtn.setEffect(btnGlow);
        confirmBtn.setOnMouseEntered(e -> confirmBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #8b2de0, #c77dff);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        ));
        confirmBtn.setOnMouseExited(e -> confirmBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #6a1fb5, #9d4edd);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        ));
        confirmBtn.setOnAction(e -> {
            confirmed.set(true);
            dialog.close();
        });

        buttons.getChildren().addAll(cancelBtn, confirmBtn);
        card.getChildren().addAll(icon, title, divider, amountBox, question, buttons);
        backdrop.getChildren().add(card);

        Scene scene = new Scene(backdrop);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();

        if (confirmed.get()) {
            doDeposit(amount);
        }
    }

    // ── Thực hiện nạp tiền ───────────────────────────────────

    private void doDeposit(double amount) {
        User user = UserSession.getCurrentUser();
        if (user == null) return;

        // Cộng vào model (memory)
        user.deposit(amount);

        // Lưu xuống DB
        boolean saved = UserDAO.updateUserMoney(
                user.getUsername(), user.getMoney(), user.getVirMoney());

        if (saved) {
            String amountStr = amount == (long) amount
                    ? "$" + (long) amount
                    : String.format("$%.2f", amount);
            showResult("✅ Nạp " + amountStr + " thành công! Số dư mới: "
                    + String.format("$%.2f", user.getMoney()), true);
            customAmountField.clear();
            clearQuickSelection();
            refreshDisplay();
        } else {
            // Rollback nếu DB lỗi
            user.deposit(-amount);
            showResult("❌ Lỗi kết nối, không thể nạp tiền. Vui lòng thử lại!", false);
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private void showInputError(String msg) {
        if (inputErrorLabel != null) {
            inputErrorLabel.setText(msg);
            inputErrorLabel.setTextFill(Color.valueOf("#ff6b6b"));
        }
    }

    private void showResult(String msg, boolean success) {
        if (resultLabel != null) {
            resultLabel.setText(msg);
            resultLabel.setTextFill(success ? Color.valueOf("#00e676") : Color.valueOf("#ff5555"));
        }
    }

    // ── Điều hướng ───────────────────────────────────────────

    @FXML
    protected void onBackToMain(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ViewNavigator.navigateTo(stage, ViewNavigator.BIDDER_HOME, "BidCycle — Trang chủ");
        } catch (IOException e) { e.printStackTrace(); }
    }
}
