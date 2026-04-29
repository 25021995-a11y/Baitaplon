package com.baitaplon.baitaplon;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
public class Product {
    private int productId;
    private String name;
    private  double curPrice;
    //private int timeRemaining;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Bidder highestBidder;
    private boolean isFinished=false;
    private boolean stop=false;
    private Seller owner;
    private double highestMaxBid = 0.0;
    private double currentStepPrice = 0.0;
    Product(int productId,String name,double price,int durationInMinutes,Seller owner){
        if(price>0){
            this.productId=productId;
            this.name=name;
            this.curPrice=price;
            this.startTime = LocalDateTime.now(); // Lấy đúng giờ hệ thống hiện tại
            this.endTime = this.startTime.plusMinutes(durationInMinutes); // Cộng thêm số phút người dùng nhập vào
            this.owner=owner;
        }
        else if(price<0){
            System.out.println("Giá tiền không được âm");
            Scanner sc=new Scanner(System.in);
            System.out.println("Nhập lại giá");
            this.curPrice= sc.nextDouble();
        }
    }
    // Trừ,cộng tiền theo giá sản phẩm đấu giá
    public int getProductId(){return this.productId;}
    public void setProductId(int productId){this.productId=productId;}
    public Seller getOwner(){
        return this.owner;
    }
    public boolean getStop(){
        return this.stop;
    }
    public void setStop(boolean b){
        this.stop=b;
    }
    public long getTimeRemaining(){
        Duration duration=Duration.between(LocalDateTime.now(),this.endTime);
        return duration.getSeconds();
    }
    //public void setTimeRemaining(int time){this.timeRemaining=time;}
    public LocalDateTime getEndTime() {
        return endTime;
    }
    public boolean isFinished() {

        if (LocalDateTime.now().isAfter(this.endTime)) {
            this.isFinished = true;
            this.stop=true;
        }
        return this.isFinished;
    }
    public void addExtraTime(long secondsToAdd) {
        this.endTime = this.endTime.plusSeconds(secondsToAdd);
    }
    public void checkAndProcessEnd(){
        if (!this.isFinished && LocalDateTime.now().isAfter(this.endTime)){
            this.isFinished = true;
            if (this.getHighestBidder() != null) {
                System.out.println("Chúc mừng " + this.getHighestBidder().getUsername() +
                        " đã mua " + this.getName() + " với giá " + this.getPrice());
                UserStorage.TruTien(this.getHighestBidder(),this);
                this.getHighestBidder().addProductToInventory(this);
                System.out.println("Số dư"+this.getHighestBidder().getMoney());
            }
            else {
                System.out.println("Sản phẩm " + this.getName() + " không ai mua");
            }
        }
    }
    public String processBid(Bidder newBidder, double amount, boolean isAutoBid, double userStepPrice) {
        if (isFinished()) {//bổ sung check tkhoan và hết thời gian
            return "Phiên đấu giá đã kết thúc!";
        }

        if (newBidder == null) {
            return "Người dùng không hợp lệ";
        }

        if (!newBidder.getIsActive()) {
            return "Tài khoản bị khóa!";
        }

        if (amount <= this.curPrice) {
            return "Lỗi: Giá đặt phải lớn hơn giá hiện tại (" + this.curPrice + ")";
        }

        if (this.highestBidder == null) {
            this.highestBidder = newBidder;
            if (isAutoBid) {
                this.highestMaxBid = amount;
                this.currentStepPrice = userStepPrice;
                return "Đã bật Auto-Bid! Bạn đang dẫn đầu với giá khởi điểm.";
            } else {
                this.curPrice = amount;
                this.highestMaxBid = 0;
                this.currentStepPrice = 0;

                if (getTimeRemaining() < 10) {
                    addExtraTime(30);
                }
                return "Đặt giá thành công! Bạn đang dẫn đầu.";
            }

        }


        if (this.highestBidder.getUserId() == newBidder.getUserId()) {
            if (isAutoBid) {
                this.highestMaxBid = Math.max(this.highestMaxBid, amount);
                this.currentStepPrice = userStepPrice;
                return "Đã cập nhật mức Auto-Bid và Bước giá mới!";
            } else {
                this.curPrice = amount;
                return "Bạn đã tự nâng mức giá hiện tại thành công!";
            }
        }

        if (this.highestMaxBid > 0) {

            if (amount <= this.highestMaxBid) {
                this.curPrice = Math.min(amount + this.currentStepPrice, this.highestMaxBid);
                return "Hạn mức thấp! Bị Auto-Bid đè. Giá hiện tại vọt lên: " + this.curPrice;

            } else {
                this.highestBidder = newBidder;

                if (isAutoBid) {
                    this.curPrice = Math.min(this.highestMaxBid + userStepPrice, amount);
                    this.highestMaxBid = amount;
                    this.currentStepPrice = userStepPrice;
                    return "Tuyệt vời! Đã vượt qua Auto-Bid cũ. Giá mới: " + this.curPrice;
                } else {
                    this.curPrice = amount;
                    this.highestMaxBid = 0;
                    this.currentStepPrice = 0;
                    return "Thành công! Đã phá vỡ Auto-Bid cũ. Bạn đang dẫn đầu!";
                }
            }

        } else {
            this.highestBidder = newBidder;

            if (isAutoBid) {
                this.curPrice = Math.min(this.curPrice + userStepPrice, amount);
                this.highestMaxBid = amount;
                this.currentStepPrice = userStepPrice;
                return "Kích hoạt Auto-Bid thành công! Đang dẫn đầu với giá: " + this.curPrice;
            } else {
                this.curPrice = amount;
                return "Đặt giá thành công! Bạn đang dẫn đầu.";
            }
        }
    }
    public String getTimeDisplay() {
        if (isFinished()) {
            return "Đã kết thúc";
        }
        // Tính khoảng cách giữa bây giờ và lúc kết thúc
        Duration duration = Duration.between(LocalDateTime.now(), this.endTime);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return minutes + " phút " + seconds + " giây";
    }
    public void setHighestBidder(Bidder name){
        this.highestBidder=name;
    }
    public void setFinished(boolean b){
        this.isFinished=b;
    }
    public boolean getFinished(){
        return this.isFinished;
    }
    public void setCurPrice(double amount){
        this.curPrice=amount;
    }
    public String getName(){
        return this.name;
    }
    public double getPrice(){
        return  this.curPrice;
    }
    public Bidder getHighestBidder(){
        return this.highestBidder;}

}
