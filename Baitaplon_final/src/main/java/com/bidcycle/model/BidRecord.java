package com.bidcycle.model;

import java.time.LocalDateTime;

/**
 * MODEL: Bản ghi một lần đặt giá.
 * Value Object đơn giản, bất biến sau khi tạo.
 */
public class BidRecord {
    private final String productName;
    private final double price;
    private final LocalDateTime time;

    public BidRecord(String productName, double price, LocalDateTime time) {
        this.productName = productName;
        this.price = price;
        this.time = time;
    }

    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public LocalDateTime getTime() { return time; }
}
