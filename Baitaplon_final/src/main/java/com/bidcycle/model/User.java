package com.bidcycle.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * MODEL: Người dùng duy nhất trong hệ thống.
 * Mỗi user vừa có thể đấu giá (bidder) vừa có thể bán hàng (seller).
 * Không được đấu giá sản phẩm do chính mình đăng.
 */
public class User {
    private int userId;
    private String username;
    private String password;
    private boolean isActive;
    private String email;
    private String gender;
    private LocalDate createdDate;

    // Tài chính
    private double money;       // Tổng số dư thực tế
    private double virMoney;    // Số dư khả dụng để đặt giá (= money - locked)

    // Kho hàng & lịch sử (in-memory)
    private ArrayList<Product> inventory;
    private ArrayList<BidRecord> bidHistory;

    // ── Constructors ──────────────────────────────────────────

    public User(int userId, String username, String password, boolean isActive,
                String email, String gender, LocalDate createdDate) {
        this.userId      = userId;
        this.username    = username;
        this.password    = password;
        this.isActive    = isActive;
        this.email       = email   != null ? email   : "";
        this.gender      = gender  != null ? gender  : "Khác";
        this.createdDate = createdDate != null ? createdDate : LocalDate.now();
        this.money    = 0;
        this.virMoney = 0;
        this.inventory  = new ArrayList<>();
        this.bidHistory = new ArrayList<>();
    }

    public User(int userId, String username, String password, boolean isActive) {
        this(userId, username, password, isActive, "", "Khác", LocalDate.now());
    }

    // ── Getters / Setters cơ bản ──────────────────────────────

    public int     getUserId()                  { return userId; }
    public void    setUserId(int userId)        { this.userId = userId; }
    public String  getUsername()                { return username; }
    public void    setUsername(String username) { this.username = username; }
    public String  getPassword()                { return password; }
    public void    setPassword(String password) { this.password = password; }
    public boolean getIsActive()                { return isActive; }
    public void    setActive(boolean active)    { this.isActive = active; }
    public String  getEmail()                   { return email != null ? email : ""; }
    public void    setEmail(String email)       { this.email = email; }
    public String  getGender()                  { return gender != null ? gender : "Khác"; }
    public void    setGender(String gender)     { this.gender = gender; }
    public LocalDate getCreatedDate()           { return createdDate != null ? createdDate : LocalDate.now(); }
    public void    setCreatedDate(LocalDate d)  { this.createdDate = d; }
    public void setId(int id) {
        this.userId = id;
    }



    public String getRoleName() { return "User"; }

    // ── Tài chính ─────────────────────────────────────────────

    /** Nạp tiền: cộng vào cả money lẫn virMoney (phần chưa bị khóa). */
    public synchronized void deposit(double amount) {
        if (amount > 0) {
            this.money    += amount;
            this.virMoney += amount;
            com.bidcycle.dao.UserDAO.updateUserMoney(this.username, this.money, this.virMoney);
        }
    }

    /** Nhận tiền từ giao dịch bán hàng (cộng cả money và virMoney). */
    public synchronized void receiveMoney(double amount) {
        if (amount > 0) {
            this.money += amount;
            this.virMoney += amount;
            com.bidcycle.dao.UserDAO.updateUserMoney(this.username, this.money, this.virMoney);
        }
    }

    public synchronized double getMoney()             { return this.money; }
    public synchronized void   setMoney(double money) { this.money = money; }

    public synchronized double getVirMoney()                { return this.virMoney; }
    public synchronized void   setVirMoney(double virMoney) { this.virMoney = virMoney; }

    /** Khóa tiền khả dụng khi đặt giá (tiền thực chưa trừ). */
    public synchronized void lockVirMoney(double amount) {
        this.virMoney = Math.max(0, this.virMoney - amount);
        // Lưu thay đổi vào DB để đồng bộ
        com.bidcycle.dao.UserDAO.updateUserMoney(this.username, this.money, this.virMoney);
    }

    /** Hoàn lại tiền khả dụng khi bị vượt giá. */
    public synchronized void unlockVirMoney(double amount) {
        this.virMoney = Math.min(this.money, this.virMoney + amount);
        // Lưu thay đổi vào DB để đồng bộ
        com.bidcycle.dao.UserDAO.updateUserMoney(this.username, this.money, this.virMoney);
    }

    /** Thanh toán thực khi thắng đấu giá (trừ tiền thật + trả tiền cho người bán). */
    public synchronized void settlePayment(Product p) {
        double price = p.getPrice();
        this.money = Math.max(0, this.money - price);
        // Lưu thay đổi của người mua vào DB
        com.bidcycle.dao.UserDAO.updateUserMoney(this.username, this.money, this.virMoney);

        User owner = p.getOwner();
        if (owner != null && owner.getUserId() != this.userId) {
            owner.receiveMoney(price);
            // Quan trọng: Lưu thay đổi của người bán vào DB
            com.bidcycle.dao.UserDAO.updateUserMoney(owner.getUsername(), owner.getMoney(), owner.getVirMoney());
        }
    }

    /** Kiểm tra tiền khả dụng có đủ để đặt giá không. */
    public boolean canAfford(double price) {
        return this.virMoney >= price;
    }

    // ── Kho hàng ──────────────────────────────────────────────

    public synchronized void addProductToInventory(Product product) {
        if (inventory == null) inventory = new ArrayList<>();
        this.inventory.add(product);
    }

    public ArrayList<Product> getInventory() {
        if (inventory == null) inventory = new ArrayList<>();
        return inventory;
    }

    // ── Lịch sử đấu giá ───────────────────────────────────────

    public synchronized void recordBid(String productName, double price) {
        if (bidHistory == null) bidHistory = new ArrayList<>();
        bidHistory.add(new BidRecord(productName, price, LocalDateTime.now()));
    }

    public ArrayList<BidRecord> getBidHistory() {
        if (bidHistory == null) bidHistory = new ArrayList<>();
        return bidHistory;
    }

    // ── Đăng bán sản phẩm ─────────────────────────────────────

    public Product createProduct(String name, double price, int durationInMinutes,
                                 String description, String imagePath) {
        return createProduct(name, price, durationInMinutes, description, imagePath, "Khác");
    }

    public Product createProduct(String name, double price, int durationInMinutes,
                                 String description, String imagePath, String category) {
        int newId = ProductStorage.generateNewProductId();
        Product product = new Product(newId, name, price, durationInMinutes, this,
                                      description, imagePath, category);
        ProductStorage.addProduct(product);
        return product;
    }
}
