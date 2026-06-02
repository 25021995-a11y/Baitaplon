package com.bidcycle.controller;

import com.bidcycle.dao.ProductDAO;
import com.bidcycle.model.Product;
import com.bidcycle.model.User;
import javafx.application.Platform;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    @FXML private LineChart<String, Number> priceCurveChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private Label  lblBidCount;
    @FXML private Label  lblPeakPrice;
    @FXML private Label  lblStartPrice;
    @FXML private Label  lblPriceIncrease;

    private Product product;
    private Timeline updateTimeline;

    // Bid history series
    private XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
    private double lastTrackedPrice = -1;
    private double startPrice = 0;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

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
        
        // Initialize Chart
        priceSeries.setName("Giá đấu");
        if (!priceCurveChart.getData().contains(priceSeries)) {
            priceCurveChart.getData().add(priceSeries);
        }

        loadProductData();
        loadInitialBidHistory();

        startLiveUpdate();
    }

    private void loadInitialBidHistory() {
        if (product == null) return;
        priceSeries.getData().clear();
        
        // Luôn bắt đầu từ giá khởi điểm
        addBidPoint(startPrice);
        
        List<double[]> history = ProductDAO.getBidHistory(product.getProductId());
        for (double[] point : history) {
            // point[1] là giá, point[0] là timestamp
            // Để biểu đồ chính xác hơn, ta nên sử dụng thời gian thực từ DB
            String timeStr = timeFormat.format(new Date((long) point[0]));
            
            // Logic step chart cho dữ liệu lịch sử
            if (!priceSeries.getData().isEmpty()) {
                XYChart.Data<String, Number> lastPoint = priceSeries.getData().get(priceSeries.getData().size() - 1);
                double lastPrice = lastPoint.getYValue().doubleValue();
                if (point[1] != lastPrice) {
                    priceSeries.getData().add(new XYChart.Data<>(getUniqueTimeStr(timeStr), lastPrice));
                }
            }
            priceSeries.getData().add(new XYChart.Data<>(getUniqueTimeStr(timeStr), point[1]));
        }
        
        if (!history.isEmpty()) {
            lastTrackedPrice = history.get(history.size() - 1)[1];
        } else {
            lastTrackedPrice = startPrice;
        }
    }

    private String getUniqueTimeStr(String timeStr) {
        String current = timeStr;
        int count = 1;
        boolean exists = true;
        List<XYChart.Data<String, Number>> dataList = new ArrayList<>(priceSeries.getData());
        while (exists) {
            exists = false;
            for (XYChart.Data<String, Number> data : dataList) {
                if (data.getXValue().equals(current)) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                current = timeStr + " (" + (count++) + ")";
            }
        }
        return current;
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

    private void addBidPoint(double price) {
        String timeStr = timeFormat.format(new Date());
        
        // Nếu đã có điểm cũ, thêm một điểm tại thời điểm mới với giá cũ 
        // để tạo đường nằm ngang (step) trước khi tăng lên giá mới
        if (!priceSeries.getData().isEmpty()) {
            XYChart.Data<String, Number> lastPoint = priceSeries.getData().get(priceSeries.getData().size() - 1);
            double lastPrice = lastPoint.getYValue().doubleValue();
            
            // Nếu giá mới khác giá cũ, thêm điểm chốt ở giá cũ tại thời điểm hiện tại
            if (price != lastPrice) {
                String stepTime = getUniqueTimeStr(timeStr);
                priceSeries.getData().add(new XYChart.Data<>(stepTime, lastPrice));
            }
        }

        String uniqueTimeStr = getUniqueTimeStr(timeStr);
        priceSeries.getData().add(new XYChart.Data<>(uniqueTimeStr, price));
        
        // Cập nhật giới hạn điểm (tăng lên để chứa được các điểm step)
        if (priceSeries.getData().size() > 100) {
            priceSeries.getData().remove(0);
        }
    }

    private void updateBidStats() {
        if (product == null) return;
        int count = priceSeries.getData().size();
        if (lblBidCount != null)
            lblBidCount.setText(count + " bids");

        double currentPrice = product.getPrice();
        double peak = priceSeries.getData().stream()
                .mapToDouble(d -> d.getYValue().doubleValue())
                .max().orElse(currentPrice);

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

                // Cập nhật biểu đồ: 
                // Khi có sự thay đổi giá hoặc để kéo dài đường nằm ngang theo thời gian
                if (currentPrice != lastTrackedPrice) {
                    addBidPoint(currentPrice);
                    lastTrackedPrice = currentPrice;
                    updateBidStats();
                } else if (!priceSeries.getData().isEmpty()) {
                    // Nếu giá không đổi, ta định kỳ thêm điểm để đường nằm ngang kéo dài ra
                    // Hoặc có thể chỉ cập nhật nhãn thời gian của điểm cuối nếu nó cùng giá
                    int lastIdx = priceSeries.getData().size() - 1;
                    XYChart.Data<String, Number> lastData = priceSeries.getData().get(lastIdx);
                    
                    String now = timeFormat.format(new Date());
                    // Nếu đã qua giây mới, cập nhật điểm cuối để kéo dài trục X
                    if (!lastData.getXValue().startsWith(now)) {
                        lastData.setXValue(getUniqueTimeStr(now));
                    }
                }
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
