package com.bidcycle.model;

/**
 * Observer nhận thông báo mỗi khi một sản phẩm có bid mới làm thay đổi giá.
 */
public interface BidObserver {
    void onNewBid(BidEvent event);
}
