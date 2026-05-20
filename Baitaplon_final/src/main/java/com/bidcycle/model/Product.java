package com.bidcycle.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * MODEL: Sản phẩm đấu giá.
 * Chứa toàn bộ logic đấu giá (xử lý giá, auto-bid, kết thúc phiên).
 * Hoàn toàn độc lập với JavaFX.
 */
public class Product {

    public static final String[] CATEGORIES = {
        "Điện tử", "Thời trang", "Đồ cổ", "Xe cộ", "Bất động sản",
        "Nghệ thuật", "Sách & Tài liệu", "Thể thao", "Khác"
    };

    private final int productId;
    private final String name;
    private final double startPrice;
    private double curPrice;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private User highestBidder;
    private boolean isFinished = false;
    private final User owner;           // người đăng bán
    private String description;
    private String imagePath;
    private String category;
    private int searchCount = 0;

    // Auto-bid state
    private User   autoBidHolder    = null;
    private double autoBidMax       = 0.0;
    private double autoBidStep      = 0.0;
    private double lockedAmountPrev = 0.0;

    // ── Constructors ──────────────────────────────────────────

    public Product(int productId, String name, double price, int durationInMinutes,
                   User owner, String description, String imagePath) {
        this(productId, name, price, durationInMinutes, owner, description, imagePath, "Khác");
    }

    public Product(int productId, String name, double price, int durationInMinutes,
                   User owner, String description, String imagePath, String category) {
        this.productId   = productId;
        this.name        = name;
        this.startPrice  = price > 0 ? price : 0;
        this.curPrice    = this.startPrice;
        this.startTime   = LocalDateTime.now();
        this.endTime     = this.startTime.plusMinutes(durationInMinutes);
        this.owner       = owner;
        this.description = description != null ? description : "";
        this.imagePath   = imagePath   != null ? imagePath   : "";
        this.category    = category    != null ? category    : "Khác";
    }
    public Product(int productId, String name, double curPrice,
                   LocalDateTime startTime, LocalDateTime endTime,User owner) {
        this.startPrice=curPrice;
        this.productId = productId;
        this.name = name;
        this.curPrice = curPrice;
        this.startTime = startTime; // Gán thẳng giá trị lấy từ DB, không gọi .now()
        this.endTime = endTime;     // Gán thẳng giá trị lấy từ DB
        this.owner=owner;
    }

    public Product(int productId, String name, double price, int durationInMinutes, User owner) {
        this(productId, name, price, durationInMinutes, owner, "", "", "Khác");
    }

    // ── Getters / Setters ─────────────────────────────────────

    public int    getProductId()              { return productId; }
    public String getName()                   { return name; }
    public User   getOwner()                  { return owner; }
    public double getStartPrice()             { return startPrice; }
    public double getPrice()                  { return curPrice; }
    public void   setCurPrice(double amount)  { this.curPrice = amount; }
    public String getDescription()            { return description != null ? description : ""; }
    public void   setDescription(String d)    { this.description = d; }
    public String getImagePath()              { return imagePath != null ? imagePath : ""; }
    public void   setImagePath(String p)      { this.imagePath = p; }
    public User   getHighestBidder()          { return highestBidder; }
    public void   setHighestBidder(User b)    { this.highestBidder = b; }
    public boolean isFinished()               { return isFinished; }
    public boolean getFinished()              { return isFinished; }
    public void   setFinished(boolean b)      { this.isFinished = b; }
    public String getCategory()               { return category != null ? category : "Khác"; }
    public void   setCategory(String c)       { this.category = c; }
    public int    getSearchCount()            { return searchCount; }
    public void   incrementSearchCount()      { this.searchCount++; }

    // ── Thời gian ─────────────────────────────────────────────

    public String getTimeDisplay() {
        if (isFinished) return "Đã kết thúc";
        Duration remaining = Duration.between(LocalDateTime.now(), endTime);
        if (remaining.isNegative() || remaining.isZero()) return "Đã kết thúc";
        long h = remaining.toHours();
        long m = remaining.toMinutesPart();
        long s = remaining.toSecondsPart();
        if (h > 0)  return String.format("%dh %02dm %02ds", h, m, s);
        if (m > 0)  return String.format("%dm %02ds", m, s);
        return String.format("%ds", s);
    }

    public void checkAndProcessEnd() {
        if (isFinished) return;
        if (LocalDateTime.now().isAfter(endTime)) {
            isFinished = true;
            if (highestBidder != null) {
                highestBidder.settlePayment(this);
                highestBidder.addProductToInventory(this);
            }
        }
    }

    // ── Đặt giá ──────────────────────────────────────────────

    /**
     * Xử lý đặt giá. Kiểm tra user không được đấu giá sản phẩm của chính mình.
     */
    public synchronized BidResult processBid(User newBidder, double amount,
                                              boolean isAutoBid, double userStep) {
        if (isFinished) return BidResult.failure("Phiên đấu giá đã kết thúc!");

        // Không được đấu giá sản phẩm của chính mình
        if (owner != null && owner.getUserId() == newBidder.getUserId()) {
            return BidResult.failure("Bạn không thể đấu giá sản phẩm của chính mình!");
        }

        if (amount <= curPrice)
            return BidResult.failure("Giá phải cao hơn giá hiện tại ($"
                + String.format("%.2f", curPrice) + ")!");
        if (!newBidder.canAfford(amount))
            return BidResult.failure(
                "Số dư khả dụng không đủ! Hiện có: $" + String.format("%.2f", newBidder.getVirMoney()));

        newBidder.recordBid(this.name, amount);

        if (highestBidder == null) {
            return setNewLeader(newBidder, amount, isAutoBid, userStep, null, 0);
        }

        // Cùng người đặt lại
        if (highestBidder.getUserId() == newBidder.getUserId()) {
            newBidder.unlockVirMoney(lockedAmountPrev);
            return setNewLeader(newBidder, amount, isAutoBid, userStep, null, 0);
        }

        // Có auto-bid đang hoạt động
        if (autoBidHolder != null && autoBidHolder.getUserId() == highestBidder.getUserId()) {
            double autoCounter = amount + autoBidStep;
            if (autoCounter <= autoBidMax) {
                newBidder.lockVirMoney(amount);
                newBidder.unlockVirMoney(amount);
                highestBidder.unlockVirMoney(lockedAmountPrev);
                curPrice = autoCounter;
                highestBidder.lockVirMoney(autoCounter);
                lockedAmountPrev = autoCounter;
                return BidResult.failure("Auto-Bid tự động phản công! Giá hiện tại: $"
                    + String.format("%.2f", curPrice));
            } else if (amount == autoBidMax) {
                curPrice = autoBidMax;
                return BidResult.failure(
                    "Giá bằng Auto-Bid tối đa. Người đang dẫn vẫn thắng theo quy tắc đặt trước.");
            } else {
                autoBidHolder.unlockVirMoney(lockedAmountPrev);
                return setNewLeader(newBidder, amount, isAutoBid, userStep, autoBidHolder, lockedAmountPrev);
            }
        }

        highestBidder.unlockVirMoney(lockedAmountPrev);
        return setNewLeader(newBidder, amount, isAutoBid, userStep, highestBidder, lockedAmountPrev);
    }

    private BidResult setNewLeader(User newBidder, double amount,
                                   boolean isAutoBid, double step,
                                   User prevLoser, double prevLockedAmount) {
        newBidder.lockVirMoney(amount);
        lockedAmountPrev = amount;
        curPrice      = amount;
        highestBidder = newBidder;

        if (isAutoBid) {
            autoBidHolder = newBidder;
            autoBidMax    = amount;
            autoBidStep   = step > 0 ? step : 1;
            return BidResult.success("✅ Auto-Bid đã kích hoạt! Bạn đang dẫn đầu với $"
                + String.format("%.2f", curPrice));
        } else {
            autoBidHolder = null;
            autoBidMax    = 0;
            autoBidStep   = 0;
            return BidResult.success("✅ Đặt giá thành công! Bạn đang dẫn đầu với $"
                + String.format("%.2f", curPrice));
        }
    }

    // ── Inner class kết quả ───────────────────────────────────

    public static class BidResult {
        public final boolean isSuccess;
        public final String  message;
        private BidResult(boolean s, String m) { isSuccess = s; message = m; }
        public static BidResult success(String m) { return new BidResult(true,  m); }
        public static BidResult failure(String m) { return new BidResult(false, m); }
    }
}
