package com.bidcycle.controller;

import com.bidcycle.model.Product;
import com.bidcycle.model.ProductStorage;
import com.bidcycle.model.User;
import com.bidcycle.model.UserSession;
import com.bidcycle.util.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CONTROLLER: My Dashboard — hiển thị tổng quan hoạt động đấu giá của người dùng.
 * Bao gồm: stat cards, biểu đồ tỉ lệ thắng, bảng xếp hạng top bidders.
 */
public class DashboardController {

    @FXML private Label lblUsername;
    @FXML private Label lblWallet;
    @FXML private Label lblTotalBids;
    @FXML private Label lblWonBids;
    @FXML private Label lblSuccessRate;
    @FXML private Label lblBalance;
    @FXML private Canvas winRateCanvas;
    @FXML private VBox   leaderboardContainer;
    @FXML private Label  lblLeaderEmpty;
    @FXML private Label  lblRecentActivity;

    @FXML
    public void initialize() {
        User user = UserSession.getCurrentUser();
        if (user != null) {
            lblUsername.setText(user.getUsername());
            lblWallet.setText(String.format("$%.2f", user.getMoney()));
            lblBalance.setText(String.format("$%.2f", user.getVirMoney()));
        }
        computeAndDisplayStats(user);
        buildLeaderboard();
        buildRecentActivity(user);
    }

    // ── Stats ─────────────────────────────────────────────────

    private void computeAndDisplayStats(User user) {
        if (user == null) {
            drawDonut(0, 0);
            return;
        }

        Collection<Product> products = ProductStorage.getAllProducts().values();

        // Tổng số lần user xuất hiện là người dẫn đầu lịch sử
        // (approximate: sản phẩm mà user là highest bidder = 1 win; total bids = search won)
        long totalParticipated = products.stream()
                .filter(p -> {
                    User hb = p.getHighestBidder();
                    return hb != null && hb.getUsername().equals(user.getUsername());
                })
                .count();

        // Phiên thắng: sản phẩm kết thúc và user là người thắng
        long won = products.stream()
                .filter(p -> p.isFinished() &&
                        p.getHighestBidder() != null &&
                        p.getHighestBidder().getUsername().equals(user.getUsername()))
                .count();

        // Tổng giao dịch: sản phẩm user đang dẫn đầu (cả đang diễn ra + kết thúc)
        long total = products.stream()
                .filter(p -> p.getHighestBidder() != null &&
                        p.getHighestBidder().getUsername().equals(user.getUsername()))
                .count();

        // Nếu không có dữ liệu thực, show demo data
        int totalBids = (int) Math.max(total, 0);
        int wonBids   = (int) Math.max(won, 0);

        lblTotalBids.setText(String.valueOf(totalBids));
        lblWonBids.setText(String.valueOf(wonBids));

        double rate = totalBids > 0 ? (wonBids * 100.0 / totalBids) : 0.0;
        lblSuccessRate.setText(String.format("%.0f%%", rate));

        drawDonut(wonBids, totalBids);
    }

    /** Vẽ biểu đồ donut tỉ lệ thắng */
    private void drawDonut(int won, int total) {
        if (winRateCanvas == null) return;
        double W = winRateCanvas.getWidth();
        double H = winRateCanvas.getHeight();
        GraphicsContext gc = winRateCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, W, H);

        double cx = W / 2, cy = H / 2;
        double outerR = Math.min(W, H) * 0.42;
        double innerR = outerR * 0.58;

        if (total == 0) {
            // Empty state — grey ring
            gc.setStroke(Color.valueOf("#2d1857"));
            gc.setLineWidth(outerR - innerR);
            gc.strokeArc(cx - (outerR + innerR) / 2, cy - (outerR + innerR) / 2,
                    outerR + innerR, outerR + innerR, 0, 360,
                    javafx.scene.shape.ArcType.OPEN);

            gc.setFill(Color.valueOf("#3a2550"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            String msg = "Chưa có";
            double tw = msg.length() * 7.5;
            gc.fillText(msg, cx - tw / 2, cy + 5);
            return;
        }

        double wonAngle   = (won * 360.0 / total);
        double lostAngle  = 360.0 - wonAngle;
        double stroke = outerR - innerR;
        double arcD  = outerR + innerR;
        double arcX  = cx - arcD / 2;
        double arcY  = cy - arcD / 2;

        // Won arc (teal)
        gc.setStroke(Color.valueOf("#00e5b0"));
        gc.setLineWidth(stroke);
        gc.strokeArc(arcX, arcY, arcD, arcD, 90, -wonAngle, javafx.scene.shape.ArcType.OPEN);

        // Lost arc (purple)
        if (lostAngle > 0.5) {
            gc.setStroke(Color.valueOf("#9d4edd"));
            gc.setLineWidth(stroke);
            gc.strokeArc(arcX, arcY, arcD, arcD, 90 - wonAngle, -lostAngle,
                    javafx.scene.shape.ArcType.OPEN);
        }

        // Glow overlay effect: inner circle mask
        gc.setFill(Color.valueOf("#160c2a"));
        gc.fillOval(cx - innerR + 1, cy - innerR + 1, (innerR - 1) * 2, (innerR - 1) * 2);

        // Center text
        double rate = won * 100.0 / total;
        String rateStr = String.format("%.0f%%", rate);
        gc.setFill(Color.valueOf("#00e5b0"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        double tw = rateStr.length() * 12;
        gc.fillText(rateStr, cx - tw / 2, cy + 7);

        gc.setFill(Color.valueOf("#6a4a8a"));
        gc.setFont(Font.font("Segoe UI", 10));
        String sub = "win rate";
        double sw = sub.length() * 5.5;
        gc.fillText(sub, cx - sw / 2, cy + 22);
    }

    // ── Leaderboard ───────────────────────────────────────────

    private void buildLeaderboard() {
        if (leaderboardContainer == null) return;
        leaderboardContainer.getChildren().clear();

        Collection<Product> products = ProductStorage.getAllProducts().values();

        // Count how many products each user is highest bidder on (all + finished)
        Map<String, long[]> stats = new LinkedHashMap<>();
        for (Product p : products) {
            User hb = p.getHighestBidder();
            if (hb == null) continue;
            String name = hb.getUsername();
            stats.computeIfAbsent(name, k -> new long[]{0, 0});
            stats.get(name)[0]++; // total leading
            if (p.isFinished()) stats.get(name)[1]++; // won
        }

        if (stats.isEmpty()) {
            if (lblLeaderEmpty != null) lblLeaderEmpty.setVisible(true);
            return;
        }

        // Sort by total leading desc
        List<Map.Entry<String, long[]>> sorted = stats.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]))
                .limit(8)
                .collect(Collectors.toList());

        String currentUser = UserSession.getCurrentUser() != null
                ? UserSession.getCurrentUser().getUsername() : "";

        String[] medals = {"🥇", "🥈", "🥉"};
        Color[] rankColors = {Color.valueOf("#ffd700"), Color.valueOf("#c0c0c0"), Color.valueOf("#cd7f32")};

        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<String, long[]> entry = sorted.get(i);
            String name  = entry.getKey();
            long total   = entry.getValue()[0];
            long won     = entry.getValue()[1];
            boolean isMe = name.equals(currentUser);

            HBox row = new HBox(0);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(9, 12, 9, 12));

            String bgColor = isMe
                    ? "-fx-background-color: #231044; -fx-border-color: #5a2d9a; -fx-border-width: 1;"
                    : (i % 2 == 0 ? "-fx-background-color: #110826;" : "-fx-background-color: #160c2a;");
            row.setStyle(bgColor + " -fx-background-radius: 8; -fx-border-radius: 8;");

            // Rank
            Label rankLbl = new Label(i < 3 ? medals[i] : "#" + (i + 1));
            rankLbl.setPrefWidth(36);
            rankLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, i < 3 ? 15 : 12));
            rankLbl.setTextFill(i < 3 ? rankColors[i] : Color.valueOf("#4a3060"));

            // Name
            Label nameLbl = new Label(isMe ? name + "  ✦" : name);
            nameLbl.setFont(Font.font("Segoe UI", isMe ? FontWeight.BOLD : FontWeight.NORMAL, 13));
            nameLbl.setTextFill(isMe ? Color.valueOf("#c77dff") : Color.valueOf("#e0caff"));
            HBox.setHgrow(nameLbl, Priority.ALWAYS);

            // Total bids
            Label totalLbl = new Label(String.valueOf(total));
            totalLbl.setPrefWidth(70);
            totalLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            totalLbl.setTextFill(Color.valueOf("#9d4edd"));

            // Won
            Label wonLbl = new Label(String.valueOf(won));
            wonLbl.setPrefWidth(60);
            wonLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            wonLbl.setTextFill(Color.valueOf("#00e5b0"));

            row.getChildren().addAll(rankLbl, nameLbl, totalLbl, wonLbl);
            leaderboardContainer.getChildren().add(row);
        }
    }

    // ── Recent activity ───────────────────────────────────────

    private void buildRecentActivity(User user) {
        if (lblRecentActivity == null || user == null) return;
        Collection<Product> products = ProductStorage.getAllProducts().values();
        List<String> activities = products.stream()
                .filter(p -> p.getHighestBidder() != null &&
                        p.getHighestBidder().getUsername().equals(user.getUsername()))
                .map(p -> {
                    String status = p.isFinished() ? "✅ Thắng" : "⚡ Đang dẫn";
                    return status + "  «" + p.getName() + "»  —  "
                            + String.format("$%.2f", p.getPrice());
                })
                .collect(Collectors.toList());

        if (activities.isEmpty()) {
            lblRecentActivity.setText("Bạn chưa tham gia phiên đấu giá nào.");
        } else {
            lblRecentActivity.setText(String.join("    |    ", activities));
        }
    }

    // ── Navigation ────────────────────────────────────────────

    @FXML protected void onNavHome(ActionEvent e)      { nav(e, ViewNavigator.BIDDER_HOME, "BidCycle — Trang chủ"); }
    @FXML protected void onNavAccount(ActionEvent e)   { nav(e, ViewNavigator.ACCOUNT,     "BidCycle - Tài khoản"); }
    @FXML protected void onNavPayment(ActionEvent e)   { nav(e, ViewNavigator.PAYMENT,     "BidCycle - Nạp tiền"); }
    @FXML protected void onNavInventory(ActionEvent e) { nav(e, ViewNavigator.INVENTORY,   "BidCycle - Kho hàng"); }
    @FXML protected void onNavSell(ActionEvent e)      { nav(e, ViewNavigator.SELLER_HOME, "BidCycle - Đăng bán"); }

    @FXML
    protected void onLogout(ActionEvent event) {
        UserSession.clearSession();
        nav(event, ViewNavigator.LOGIN, "BidCycle - Đăng nhập");
    }

    private void nav(ActionEvent event, String fxml, String title) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ViewNavigator.navigateTo(stage, fxml, title);
        } catch (IOException e) { e.printStackTrace(); }
    }
}
