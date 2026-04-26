package com.baitaplon.baitaplon;

import javafx.fxml.FXML;

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
    public  boolean getIsActive(){return this.isActive;}
    public void setActive(boolean active){this.isActive=active;}
    public abstract void showRole();

}
//chưa viết các chưức năng cho admin:kick,xóa tài khoản,...
class Admin extends User{
    Admin(int userId, String username, String password, boolean isActive){
        super(userId,username,password,isActive);
    }
    @Override
    public void showRole() {
        System.out.println("Vai trò:Admin");
    }
}
//Bổ sung seller, nhận được tiền đấu giá từ bidder
class Seller extends User{
    private double money;
    Seller(int userId, String username, String password, boolean isActive){
        super(userId,username,password,isActive);
        this.money=0.0;
    }
    public void setMoney(double money){ this.money=money; }
    public double getMoney(){
        return this.money;
    }
    public void receiveMoney(double amount){
        this.money+=amount;
        System.out.println("Seller:"+this.getUsername()+" vừa nhận được :"+amount+" Tổng tiền:"+this.money);
    }
    public void pushItem(String name,double price,int time){
        System.out.println("Seller "+this.getUsername()+" đang bán: "+name+" giá "+price);
        int newId=ProductStorage.generateNewProductId();
        Product product=new Product(newId,name,price,time,this);
        ProductStorage.addProduct(product);
    }

    @Override
    public void showRole() {
        System.out.println("Vai trò:Seller");
    }
}
//Thay đổi phần trừ tền khi tham gia đấu giá
class Bidder extends User{
    private double money;
    private double virMoney;
    private ArrayList<Product> inv;
    public Bidder(int userId, String username, String password, boolean isActive){
        super(userId,username,password,isActive);
        this.inv=new ArrayList<Product>();
    }
    public void deposit(double amount){
        this.money+=amount;
        this.virMoney+=amount;
        System.out.println("Nạp thành công:"+amount);
    }
    public double getMoney(){
        return this.money;
    }
    public void setVirMoney(double virMoney){ this.virMoney=virMoney; }
    public boolean bidding(Product p,double price){
        if(price>this.getMoney()){
            System.out.println("Số dư không khả dụng");
            return false;
        }
        else if(price<=p.getPrice()){
            System.out.println("Không đủ tiền");
            return false;
        }
        return true;
    }
    public void setMoney(double money){
        this.money=money;
    }
    public double getVirMoney(){
        return this.virMoney;
    }
    public void virMoneyPay(double amount){
        this.virMoney-=amount;
    }
    public void ThanhToan(Product p){
        double priceToPay=p.getPrice();
        this.money-=priceToPay;
        Seller owner=p.getOwner();
        if (owner !=null){
            owner.receiveMoney(priceToPay);
        }
        System.out.println("Bidder: "+this.getUsername()+" thanh toán "+priceToPay+" cho Seller "+owner.getUsername());

    }
    public void addProductToInventory(Product product) {
        this.inv.add(product);
        System.out.println("Đã thêm: "+product.getName()+" vào túi đồ của "+this.getUsername());
    }
    public ArrayList<Product> getInv(){
        return this.inv;
    }
    @Override
    public void showRole() {
        System.out.println("Vai trò:Bidder");
    }

}