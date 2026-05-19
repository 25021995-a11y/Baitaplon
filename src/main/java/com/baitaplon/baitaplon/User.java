package com.baitaplon.baitaplon;

import java.util.ArrayList;

public abstract class User {
    private int userId;
    private String username;
    private String password;
    private boolean isActive;

    public User(int userId, String username, String password, boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.isActive = isActive;
    }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean getIsActive() { return this.isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public abstract void showRole();
}

class Admin extends User {
    Admin(int userId, String username, String password, boolean isActive){
        super(userId, username, password, isActive);
    }
    @Override
    public void showRole() {
        System.out.println("Vai trò: Admin");
    }

    public void deactivateUser(User user){
        user.setActive(false);
        System.out.println("Đã khóa tài khoản: " + user.getUsername());
    }
}

class Seller extends User {
    private double money = 0;

    Seller(int userId, String username, String password, boolean isActive){
        super(userId, username, password, isActive);
    }

    public synchronized void setMoney(double money) {
        this.money = money;
    }

    @Override
    public void showRole() { System.out.println("Vai trò: Seller"); }

    // THÊM SYNCHRONIZED: Đảm bảo an toàn khi nhận tiền từ nhiều Bidder
    public synchronized void receiveMoney(double amount) {
        this.money += amount;
    }

    public synchronized double getMoney() { return this.money; }

    public void pushItem(String name, double price, int durationInMinutes){
        int newId = ProductStorage.generateNewProductId();
        Product newProduct = new Product(newId, name, price, durationInMinutes, this);
        ProductStorage.addProduct(newProduct);
        System.out.println("Seller " + this.getUsername() + " đã đăng bán: " + name);
    }
}

class Bidder extends User {
    private double money;
    private double virMoney; // Tiền ảo bị phong tỏa
    private ArrayList<Product> inv;

    Bidder(int userId, String username, String password, boolean isActive){
        super(userId, username, password, isActive);
        this.money = 0;
        this.virMoney = 0;
        this.inv = new ArrayList<>();
    }

    @Override
    public void showRole() { System.out.println("Vai trò: Bidder"); }

    // THÊM SYNCHRONIZED: An toàn khi nạp tiền
    public synchronized void deposit(double amount){
        if(amount > 0){
            this.money += amount;
            this.virMoney += amount;
            System.out.println("Nạp thành công: " + amount);
        }
    }

    public synchronized double getMoney() { return this.money; }
    public synchronized void setMoney(double money) { this.money = money; }

    public synchronized double getVirMoney() { return this.virMoney; }
    public synchronized void setVirMoney(double virMoney){ this.virMoney = virMoney; }

    public synchronized void virMoneyPay(double amount){ this.virMoney -= amount; }

    public boolean bidding(Product p, double price){
        if(price > this.getMoney()){
            System.out.println("Số dư không khả dụng");
            return false;
        } else if(price <= p.getPrice()){
            System.out.println("Giá tiền phải lớn hơn giá hiện tại!");
            return false;
        }
        return true;
    }

    // THÊM SYNCHRONIZED: Giao dịch thanh toán an toàn luồng
    public synchronized void ThanhToan(Product p){
        double priceToPay = p.getPrice();
        this.money -= priceToPay;
        Seller owner = p.getOwner();
        if (owner != null){
            owner.receiveMoney(priceToPay);
        }
        System.out.println("Bidder: " + this.getUsername() + " thanh toán " + priceToPay + " cho Seller " + owner.getUsername());
    }

    public synchronized void addProductToInventory(Product product) {
        this.inv.add(product);
        System.out.println("Đã thêm: " + product.getName() + " vào túi đồ của " + this.getUsername());
    }

    public ArrayList<Product> getInv() { return inv; }
}