package com.bidcycle.controller;

import com.bidcycle.model.BidEvent;
import com.bidcycle.model.BidObserver;
import com.bidcycle.model.Product;
import com.bidcycle.model.User;
import javafx.application.Platform;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProductDetailController implements BidObserver {

    @FXML private Label     lblProductName;
    @FXML private Label     lblSeller;
    @FXML private Label     lblCurrentPrice;
    @FXML private Label     lblTimeLeft;
    @FXML private Label     lblTopBidder;
    @FXML private Label     lblDescription;
    @FXML private ImageView productImage;
    @FXML private Label     lblNoImage;
    @FXML private Canvas    priceChartCanvas;

    // Bid History chart widgets
    @FXML private Canvas bidHistoryCanvas;
    @FXML private Label  lblBidCount;
    @FXML private Label  lblPeakPrice;
    @FXML private Label  lblStartPrice;
    @FXML private Label  lblPriceIncrease;

    private Product product;
    private Timeline updateTimeline;

    // Bid history: list of (timestamp millis, price)
    private final List<double[]> bidHistory = new ArrayList<>();
    private double lastTrackedPrice = -1;
    private double startPrice = 0;

    public void setProduct(Product p) {
        if (updateTimeline != null) {
            updateTimeline.stop();
        }
        if (this.product != null) {
            this.product.removeBidObserver(this);
        }
        this.product = p;
        this.product.addBidObserver(this);
        this.startPrice = p.getStartPrice();
        loadProductData();
        // Seed the chart with current price as starting point
        addBidPoint(p.getPrice());
        lastTrackedPrice = p.getPrice();
        startLiveUpdate();
    }

    @Override
    public void onNewBid(BidEvent event) {
        if (product == null || event.getProduct() != product) return;
        Platform.runLater(() -> {
            addBidPoint(event.getAmount());
            lastTrackedPrice = event.getAmount();
            loadProductData();
            drawBidHistoryChart();
        });
    }

    private void loadProductData() {
        if (product == null) return;
        lblProductName.setText(product.getName());
        lblSeller.setText("Người bán: " + (product.getOwner() != null ? product.getOwner().getUsername() : "N/A"));
        lblCurrentPrice.setText(String.format("$%.2f", product.getPrice()));
        lblTimeLeft.setText(product.getTimeDisplay());
        User hb = product.getHighestBidder();
        lblTopBidder.setText(hb != null ? "🏆 " + hb.getUsername() : "Chưa có ai đặt giá");
        String desc = product.getDescription();
        lblDescription.setText((desc != null && !desc.isEmpty()) ? desc : "Không có mô tả.");
        loadProductImage();
        drawPriceIndicator();
        updateBidStats();
    }

    private void loadProductImage() {
        String imgPath = product.getImagePath();
        if (imgPath != null && !imgPath.isEmpty()) {
            try {
                File f = new File(imgPath);
                if (f.exists()) {
                    productImage.setImage(new Image(f.toURI().toString()));
                    productImage.setVisible(true);
                    if (lblNoImage != null) lblNoImage.setVisible(false);
                    return;
                }
            } catch (Exception ignored) {}
        }
        productImage.setVisible(false);
        if (lblNoImage != null) lblNoImage.setVisible(true);
    }

    private void drawPriceIndicator() {
        if (priceChartCanvas == null || product == null) return;
        double W = priceChartCanvas.getWidth(), H = priceChartCanvas.getHeight();
        GraphicsContext gc = priceChartCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, W, H);
        gc.setFill(Color.valueOf("#160c2a")); gc.fillRoundRect(0, 0, W, H, 10, 10);
        gc.setFill(Color.valueOf("#9d4edd44")); gc.fillRoundRect(10, 10, W-20, H-20, 8, 8);
        gc.setFill(Color.valueOf("#c77dff")); gc.setFont(Font.font("Segoe UI Bold", 13));
        gc.fillText("💰 Giá hiện tại: " + String.format("$%.2f", product.getPrice()), 20, H/2+5);
    }

    // ── Bid History Chart ─────────────────────────────────────

    /** Ghi nhận một điểm giá mới vào lịch sử. */
    private void addBidPoint(double price) {
        bidHistory.add(new double[]{System.currentTimeMillis(), price});
        // Giữ tối đa 60 điểm để chart không quá rộng
        if (bidHistory.size() > 60) bidHistory.remove(0);
    }

    /** Vẽ biểu đồ đường lịch sử giá theo thời gian thực. */
    private void drawBidHistoryChart() {
        if (bidHistoryCanvas == null) return;
        double W = bidHistoryCanvas.getWidth();
        double H = bidHistoryCanvas.getHeight();
        GraphicsContext gc = bidHistoryCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, W, H);

        // Background
        gc.setFill(Color.valueOf("#0a0618"));
        gc.fillRoundRect(0, 0, W, H, 10, 10);

        int n = bidHistory.size();
        if (n < 2) {
            // Chưa đủ dữ liệu
            gc.setFill(Color.valueOf("#3a2550"));
            gc.setFont(Font.font("Segoe UI", 11));
            gc.fillText("Đang chờ dữ liệu đấu giá...", 10, H / 2 + 4);
            return;
        }

        double pad = 14;
        double chartW = W - pad * 2;
        double chartH = H - pad * 2;

        // Price range
        double minP = bidHistory.stream().mapToDouble(d -> d[1]).min().orElse(0);
        double maxP = bidHistory.stream().mapToDouble(d -> d[1]).max().orElse(minP + 1);
        if (maxP == minP) { maxP = minP + 1; }

        // Time range
        double t0 = bidHistory.get(0)[0];
        double t1 = bidHistory.get(n - 1)[0];
        if (t1 == t0) t1 = t0 + 1;

        // Grid lines (horizontal)
        gc.setStroke(Color.valueOf("#1e1035"));
        gc.setLineWidth(0.8);
        for (int i = 0; i <= 4; i++) {
            double y = pad + chartH * i / 4.0;
            gc.strokeLine(pad, y, W - pad, y);
        }

        // Y-axis price labels
        gc.setFill(Color.valueOf("#3a2550"));
        gc.setFont(Font.font("Segoe UI", 8));
        for (int i = 0; i <= 4; i++) {
            double price = maxP - (maxP - minP) * i / 4.0;
            double y = pad + chartH * i / 4.0;
            gc.fillText(String.format("$%.0f", price), 0, y + 3);
        }

        // Area fill under the line (gradient effect via opacity)
        double[] xPts = new double[n + 2];
        double[] yPts = new double[n + 2];
        for (int i = 0; i < n; i++) {
            double tx = (bidHistory.get(i)[0] - t0) / (t1 - t0);
            double ty = (bidHistory.get(i)[1] - minP) / (maxP - minP);
            xPts[i] = pad + tx * chartW;
            yPts[i] = pad + chartH - ty * chartH;
        }
        xPts[n]     = xPts[n - 1];
        yPts[n]     = pad + chartH;
        xPts[n + 1] = pad;
        yPts[n + 1] = pad + chartH;

        gc.setFill(Color.valueOf("#9d4edd22"));
        gc.fillPolygon(xPts, yPts, n + 2);

        // Line
        gc.setStroke(Color.valueOf("#9d4edd"));
        gc.setLineWidth(1.8);
        gc.beginPath();
        gc.moveTo(xPts[0], yPts[0]);
        for (int i = 1; i < n; i++) {
            gc.lineTo(xPts[i], yPts[i]);
        }
        gc.stroke();

        // Highlight: last point (most recent bid)
        double lx = xPts[n - 1], ly = yPts[n - 1];
        gc.setFill(Color.valueOf("#00e5b0"));
        gc.fillOval(lx - 4, ly - 4, 8, 8);
        // Outer glow ring
        gc.setStroke(Color.valueOf("#00e5b066"));
        gc.setLineWidth(2);
        gc.strokeOval(lx - 7, ly - 7, 14, 14);

        // Current price label near last point
        gc.setFill(Color.valueOf("#00e5b0"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        String priceTag = String.format("$%.0f", bidHistory.get(n - 1)[1]);
        double labelX = lx + 8 > W - 30 ? lx - 35 : lx + 6;
        gc.fillText(priceTag, labelX, ly - 4);

        // Vertical dashed cursor line at last point
        gc.setStroke(Color.valueOf("#9d4edd55"));
        gc.setLineWidth(1);
        gc.setLineDashes(3, 3);
        gc.strokeLine(lx, pad, lx, pad + chartH);
        gc.setLineDashes();
    }

    private void updateBidStats() {
        if (product == null) return;
        int count = bidHistory.size();
        if (lblBidCount != null)
            lblBidCount.setText(Math.max(0, count - 1) + " bids");

        double currentPrice = product.getPrice();
        double peak = bidHistory.stream().mapToDouble(d -> d[1]).max().orElse(currentPrice);

        if (lblPeakPrice != null)  lblPeakPrice.setText(String.format("$%.2f", peak));
        if (lblStartPrice != null) lblStartPrice.setText(String.format("$%.2f", startPrice));
        if (lblPriceIncrease != null) {
            double inc = peak - startPrice;
            String sign = inc >= 0 ? "+" : "";
            lblPriceIncrease.setText(sign + String.format("$%.2f", inc));
        }
    }

    private void startLiveUpdate() {
        updateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (product != null) {
                double currentPrice = product.getPrice();
                lblCurrentPrice.setText(String.format("$%.2f", currentPrice));
                lblTimeLeft.setText(product.getTimeDisplay());
                User hb = product.getHighestBidder();
                lblTopBidder.setText(hb != null ? "🏆 " + hb.getUsername() : "Chưa có ai đặt giá");
                drawPriceIndicator();

                // Chỉ thêm điểm mới khi giá thay đổi → biểu đồ tự cập nhật
                if (currentPrice != lastTrackedPrice) {
                    addBidPoint(currentPrice);
                    lastTrackedPrice = currentPrice;
                }
                drawBidHistoryChart();
                updateBidStats();
            }
        }));
        updateTimeline.setCycleCount(Animation.INDEFINITE);
        updateTimeline.play();
    }

    @FXML
    protected void onClose() {
        dispose();
        Stage stage = (Stage) lblProductName.getScene().getWindow();
        stage.close();
    }

    public void dispose() {
        if (updateTimeline != null) updateTimeline.stop();
        if (product != null) product.removeBidObserver(this);
    }
}
