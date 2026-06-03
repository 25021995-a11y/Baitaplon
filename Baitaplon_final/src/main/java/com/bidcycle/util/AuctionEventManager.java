package com.bidcycle.util;

import com.bidcycle.model.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Subject trong Observer Pattern. 
 * Quản lý các observer và thông báo khi có sự kiện đấu giá mới.
 */
public class AuctionEventManager {
    private static AuctionEventManager instance;
    
    // Map từ productId đến danh sách các observer đang quan tâm sản phẩm đó
    private final Map<Integer, List<AuctionObserver>> observersMap = new ConcurrentHashMap<>();

    private AuctionEventManager() {}

    public static synchronized AuctionEventManager getInstance() {
        if (instance == null) {
            instance = new AuctionEventManager();
        }
        return instance;
    }

    /**
     * Đăng ký observer cho một sản phẩm cụ thể.
     */
    public void subscribe(int productId, AuctionObserver observer) {
        observersMap.computeIfAbsent(productId, k -> new CopyOnWriteArrayList<>()).add(observer);
    }

    /**
     * Hủy đăng ký observer.
     */
    public void unsubscribe(int productId, AuctionObserver observer) {
        List<AuctionObserver> observers = observersMap.get(productId);
        if (observers != null) {
            observers.remove(observer);
            if (observers.isEmpty()) {
                observersMap.remove(productId);
            }
        }
    }

    /**
     * Thông báo cho tất cả observer đang theo dõi sản phẩm này.
     */
    public void notifyBid(Product product) {
        // Cập nhật giá mới nhất vào Watcher để tránh việc Watcher notify ngược lại chính nó
        DatabaseWatcher.getInstance().recordLocalUpdate(product.getProductId(), product.getPrice());

        List<AuctionObserver> observers = observersMap.get(product.getProductId());
        if (observers != null) {
            for (AuctionObserver observer : observers) {
                observer.onBidUpdate(product);
            }
        }
    }
}
