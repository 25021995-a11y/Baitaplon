package com.baitaplon.baitaplon;

import java.util.HashMap;
import java.util.Map;

public class ProductStorage {
    // Lưu sản phẩm theo ID
    private final static Map<Integer, Product> productMap = new HashMap<>();

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

    //tìm sản phẩm theo ID
    public static Product getProductById(int id) {
        return productMap.get(id);
    }

    // xóa sản phẩm
    public static void removeProduct(int id) {
        if (productMap.containsKey(id)) {
            productMap.remove(id);
            System.out.println("Đã xóa sản phẩm ID: " + id);
        } else {
            System.out.println("Không tìm thấy sản phẩm");
        }
    }
}