package com.bidcycle.model;

import java.time.LocalDateTime;

/**
 * Dữ liệu sự kiện được gửi từ Product đến các observer khi có bid mới.
 */
public class BidEvent {
    private final Product product;
    private final User bidder;
    private final double amount;
    private final boolean autoBid;
    private final LocalDateTime time;

    public BidEvent(Product product, User bidder, double amount, boolean autoBid) {
        this.product = product;
        this.bidder = bidder;
        this.amount = amount;
        this.autoBid = autoBid;
        this.time = LocalDateTime.now();
    }

    public Product getProduct() { return product; }
    public User getBidder() { return bidder; }
    public double getAmount() { return amount; }
    public boolean isAutoBid() { return autoBid; }
    public LocalDateTime getTime() { return time; }
}
