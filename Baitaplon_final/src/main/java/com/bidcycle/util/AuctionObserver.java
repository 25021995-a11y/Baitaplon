package com.bidcycle.util;

import com.bidcycle.model.Product;

/**
 * Interface cho Observer Pattern để cập nhật Realtime Update.
 */
public interface AuctionObserver {
    /**
     * Gọi khi có một sản phẩm được cập nhật (đặt giá mới).
     * @param product Sản phẩm vừa được đặt giá.
     */
    void onBidUpdate(Product product);
}
