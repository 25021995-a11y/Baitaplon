package com.baitaplon.baitaplon;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

public class Product {
    private int productId;
    private String name;
    private double curPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Bidder highestBidder;
    private boolean isFinished = false;
    private boolean stop = false;
    private Seller owner;

    // Các biến phục vụ Auto-bidding
    private double highestMaxBid = 0.0;
    private double currentStepPrice = 0.0;

    public Product(int productId, String name, double price, int durationInMinutes, Seller owner) {
        if (price > 0) {
            this.productId = productId;
            this.name = name;
            this.curPrice = price;
            this.startTime = LocalDateTime.now();
            this.endTime = this.startTime.plusMinutes(durationInMinutes);
            this.owner = owner;
        } else {
            System.out.println("Giá tiền không được âm");
            Scanner sc = new Scanner(System.in);
            System.out.println("Nhập lại giá: ");
            this.curPrice = sc.nextDouble();
        }
    }

    // --- Các Getters/Setters cơ bản (Đảm bảo JavaFX gọi không bị lỗi) ---
    public int getProductId() { return this.productId; }
    public Seller getOwner() { return this.owner; }
    public double getPrice() { return this.curPrice; }
    public void setCurPrice(double amount) { this.curPrice = amount; }
    public String getName() { return this.name; }

    public Bidder getHighestBidder() { return this.highestBidder; }
    public void setHighestBidder(Bidder name) { this.highestBidder = name; }

    // Đã sửa lỗi: Thêm cả isFinished() và getFinished() để không bị lỗi ở các nơi gọi khác nhau
    public boolean getFinished() { return this.isFinished; }
    public boolean isFinished() { return this.isFinished; }
    public void setFinished(boolean b) { this.isFinished = b; }

    public long getTimeRemaining() {
        Duration duration = Duration.between(LocalDateTime.now(), this.endTime);
        return Math.max(0, duration.getSeconds());
    }

    public void addExtraTime(long secondsToAdd) {
        this.endTime = this.endTime.plusSeconds(secondsToAdd);
    }

    public String getTimeDisplay() {
        if (this.isFinished) { // Đã fix lỗi ở đây
            return "Đã kết thúc";
        }
        Duration duration = Duration.between(LocalDateTime.now(), this.endTime);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();

        if (minutes < 0 || seconds < 0) return "Đã kết thúc"; // Chống hiển thị số âm
        return minutes + " phút " + seconds + " giây";
    }

    // SYNCHRONIZED: An toàn khi nhiều người check thời gian cùng lúc
    public synchronized void checkAndProcessEnd() {
        if (!this.isFinished && LocalDateTime.now().isAfter(this.endTime)) {
            this.isFinished = true;
            this.stop = true;
            if (this.highestBidder != null) {
                System.out.println("Chúc mừng " + this.highestBidder.getUsername() + " đã mua " + this.name + " với giá " + this.curPrice);
                UserStorage.TruTien(this.highestBidder, this);
                this.highestBidder.addProductToInventory(this);
            } else {
                System.out.println("Sản phẩm " + this.name + " không ai mua");
            }
        }
    }

    // SYNCHRONIZED: Đóng gói toàn bộ logic Đấu giá an toàn đa luồng
    public synchronized String processBid(Bidder newBidder, double amount, boolean isAutoBid, double userStepPrice) {
        if (this.isFinished) return "Phiên đấu giá đã kết thúc!";
        if (amount <= this.curPrice) return "Lỗi: Giá đặt phải lớn hơn giá hiện tại (" + this.curPrice + ")";

        // Anti-Sniping: Tranh cướp giờ chót
        if (getTimeRemaining() < 10) {
            addExtraTime(30);
            System.out.println("Kích hoạt Anti-Sniping: +30 giây cho " + this.name);
        }

        if (this.highestBidder == null) {
            this.highestBidder = newBidder;
            if (isAutoBid) {
                this.highestMaxBid = amount;
                this.currentStepPrice = userStepPrice;
                return "Đã bật Auto-Bid! Bạn đang dẫn đầu.";
            } else {
                this.curPrice = amount;
                return "Đặt giá thành công! Bạn đang dẫn đầu.";
            }
        }

        // Cạnh tranh Auto-Bid
        if (this.highestMaxBid > 0 && this.highestBidder.getUserId() != newBidder.getUserId()) {
            if (amount <= this.highestMaxBid) {
                this.curPrice = Math.min(amount + this.currentStepPrice, this.highestMaxBid);
                return "Giá của bạn (" + amount + ") đã bị Auto-Bid của người khác vượt qua (" + this.curPrice + ")!";
            } else {
                this.highestBidder = newBidder;
                this.curPrice = amount;
                this.highestMaxBid = isAutoBid ? amount : 0;
                this.currentStepPrice = isAutoBid ? userStepPrice : 0;
                return "Bạn đã vượt qua Auto-Bid cũ và đang dẫn đầu!";
            }
        } else {
            this.highestBidder = newBidder;
            this.curPrice = amount;
            if (isAutoBid) {
                this.highestMaxBid = amount;
                this.currentStepPrice = userStepPrice;
                return "Kích hoạt Auto-Bid thành công!";
            }
            return "Đặt giá thành công!";
        }
    }
}