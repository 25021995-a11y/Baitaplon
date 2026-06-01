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
    private java.util.List<AutoBidConfig> autoBids = new java.util.ArrayList<>();
    private double lockedAmountPrev = 0.0;

    public java.util.List<AutoBidConfig> getAutoBids() { return autoBids; }
    public void setAutoBids(java.util.List<AutoBidConfig> list) { this.autoBids = list; }

    public static class AutoBidConfig {
        private final User user;
        private final double maxBid;
        private final double increment;
        private final LocalDateTime createdAt;

        public AutoBidConfig(User user, double maxBid, double increment) {
            this(user, maxBid, increment, LocalDateTime.now());
        }
        public AutoBidConfig(User user, double maxBid, double increment, LocalDateTime createdAt) {
            this.user = user;
            this.maxBid = maxBid;
            this.increment = increment;
            this.createdAt = createdAt;
        }
        public User getUser() { return user; }
        public double getMaxBid() { return maxBid; }
        public double getIncrement() { return increment; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
    public double getLockedAmountPrev() { return lockedAmountPrev; }
    public void setLockedAmountPrev(double a) { this.lockedAmountPrev = a; }

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
            // Cập nhật trạng thái kết thúc vào DB
            com.bidcycle.dao.ProductDAO.updateProductFinished(this.productId, true);

            if (highestBidder != null) {
                highestBidder.settlePayment(this);
                highestBidder.addProductToInventory(this);
            }
        }
    }

    // ── Đặt giá ──────────────────────────────────────────────

    /**
     * Xử lý đặt giá. Hỗ trợ nhiều người Auto-Bid đồng thời.
     */
    public synchronized BidResult processBid(User newBidder, double amount,
                                              boolean isAutoBid, double userStep) {
        if (isFinished) return BidResult.failure("Phiên đấu giá đã kết thúc!");

        if (owner != null && owner.getUserId() == newBidder.getUserId()) {
            return BidResult.failure("Bạn không thể đấu giá sản phẩm của chính mình!");
        }

        // 1. Cập nhật hoặc thêm cấu hình Auto-Bid của người dùng này
        if (isAutoBid) {
            if (!newBidder.canAfford(amount)) {
                return BidResult.failure("Số dư không đủ để đặt mức giá tối đa: $" + String.format("%.2f", amount));
            }
            autoBids.removeIf(config -> config.getUser().getUserId() == newBidder.getUserId());
            autoBids.add(new AutoBidConfig(newBidder, amount, userStep));
        }

        // 2. Logic đấu giá chính
        // Nếu không phải auto-bid, kiểm tra giá đặt cao hơn giá hiện tại
        if (!isAutoBid && amount <= curPrice) {
            return BidResult.failure("Giá phải cao hơn giá hiện tại ($" + String.format("%.2f", curPrice) + ")!");
        }
        
        // Nếu là bid thủ công, xử lý như một bid đơn lẻ và kích hoạt auto-bid competition
        // Nếu là đăng ký auto-bid mới, nó cũng có thể kích hoạt competition nếu maxBid > curPrice

        return resolveCompetition(newBidder, amount, isAutoBid);
    }

    private BidResult resolveCompetition(User triggerUser, double triggerAmount, boolean wasAutoReg) {
        // Thuật toán: 
        // Lặp cho đến khi không còn sự thay đổi về người dẫn đầu hoặc giá.
        // Trong mỗi vòng, tìm người có thể đặt giá cao nhất dựa trên luật:
        // - Người đang dẫn đầu (highestBidder) giữ vị thế.
        // - Những người khác (bao gồm triggerUser nếu không phải highestBidder) cố gắng vượt qua.
        // - Ưu tiên người đăng ký auto-bid trước.

        boolean changed = true;
        String message = "";
        
        // Nếu chưa có ai dẫn đầu, gán tạm triggerUser nếu đủ điều kiện
        if (highestBidder == null) {
            double startBid = Math.max(startPrice, triggerAmount); 
            // Nếu là đăng ký auto-bid, giá khởi điểm có thể là startPrice
            if (wasAutoReg) startBid = startPrice;
            
            setNewLeader(triggerUser, startBid);
            message = "✅ Bạn đang dẫn đầu!";
        } else if (!wasAutoReg && triggerAmount > curPrice) {
            // Bid thủ công vượt giá hiện tại
            if (highestBidder.getUserId() != triggerUser.getUserId()) {
                highestBidder.unlockVirMoney(lockedAmountPrev);
            }
            setNewLeader(triggerUser, triggerAmount);
            message = "✅ Bạn đang dẫn đầu!";
        }

        while (changed) {
            changed = false;
            
            // Tìm đối thủ tiềm năng có thể vượt qua highestBidder
            // Bao gồm những người trong danh sách autoBids
            
            // Sắp xếp autoBids theo thời gian đăng ký để đảm bảo ưu tiên người đăng ký trước
            autoBids.sort(java.util.Comparator.comparing(AutoBidConfig::getCreatedAt));

            for (AutoBidConfig config : autoBids) {
                User opponent = config.getUser();
                if (highestBidder != null && opponent.getUserId() == highestBidder.getUserId()) continue;

                // Đối thủ cố gắng vượt qua curPrice
                double nextBid = curPrice + config.getIncrement();
                
                // Nếu giá khởi điểm của sản phẩm chưa bị ai bid (chưa có highestBidder thực sự)
                // Hoặc opponent muốn nhảy vào
                
                if (nextBid <= config.getMaxBid()) {
                    // Đối thủ có thể nâng giá
                    if (highestBidder != null) {
                        highestBidder.unlockVirMoney(lockedAmountPrev);
                    }
                    setNewLeader(opponent, nextBid);
                    changed = true;
                    message = "Auto-Bid phản công! " + opponent.getUsername() + " đang dẫn đầu với $" + String.format("%.2f", curPrice);
                } else if (curPrice < config.getMaxBid()) {
                    // Đối thủ không thể cộng thêm bước giá nhưng có thể lên tới maxBid
                    if (highestBidder != null) {
                        highestBidder.unlockVirMoney(lockedAmountPrev);
                    }
                    setNewLeader(opponent, config.getMaxBid());
                    changed = true;
                    message = "Auto-Bid phản công! " + opponent.getUsername() + " đang dẫn đầu với $" + String.format("%.2f", curPrice);
                }
            }
        }

        if (highestBidder != null && highestBidder.getUserId() == triggerUser.getUserId()) {
            return BidResult.success(wasAutoReg ? "✅ Đã kích hoạt Auto-Bid thành công!" : "✅ Đặt giá thành công!");
        } else {
            return BidResult.failure(message.isEmpty() ? "Giá của bạn đã bị vượt qua!" : message);
        }
    }

    private void setNewLeader(User newBidder, double amount) {
        newBidder.lockVirMoney(amount);
        lockedAmountPrev = amount;
        curPrice      = amount;
        highestBidder = newBidder;
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
