package com.bidcycle.model;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MODEL: Kho lưu trữ sản phẩm trong bộ nhớ (In-memory Store).
 * Đây là tầng Model, không phải Repository/DAO vì không truy cập DB.
 * Dùng ConcurrentHashMap + AtomicInteger để đảm bảo thread-safety.
 */
public class ProductStorage {

    private static final Map<Integer, Product> productMap = new ConcurrentHashMap<>();
    private static final AtomicInteger idCounter = new AtomicInteger(0);

    private ProductStorage() {} // Không cho phép khởi tạo

    public static int generateNewProductId() {
        return idCounter.incrementAndGet();
    }

    public static void addProduct(Product product) {
        productMap.put(product.getProductId(), product);
    }

    public static Map<Integer, Product> getAllProducts() {
        return Collections.unmodifiableMap(productMap);
    }

    public static Product getProductById(int id) {
        return productMap.get(id);
    }

    public static void removeProduct(int id) {
        if (productMap.remove(id) != null) {
            System.out.println("Đã xóa sản phẩm ID: " + id);
        } else {
            System.out.println("Không tìm thấy sản phẩm ID: " + id);
        }
    }
}
