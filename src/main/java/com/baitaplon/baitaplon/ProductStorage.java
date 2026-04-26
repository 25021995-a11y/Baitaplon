package com.baitaplon.baitaplon;

import java.util.HashMap;
import java.util.Map;

public class ProductStorage {
    // Lưu sản phẩm theo ID
    private static Map<Integer, Product> productMap = new HashMap<>();

    // Bộ đếm ID cho sản phẩm
    private static int currentMaxProductId = 0;

    // Hàm cấp phát ID mới cho Sản phẩm
    public static int generateNewProductId() {
        currentMaxProductId++;
        return currentMaxProductId;
    }

    // Thêm sản phẩm vào kho
    public static void addProduct(Product product) {
        productMap.put(product.getProductId(), product);
    }

    // Hàm lấy toàn bộ sản phẩm (dùng để hiển thị lên bảng)
    public static Map<Integer, Product> getAllProducts() {
        return productMap;
    }
}