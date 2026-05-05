package com.baitaplon.baitaplon;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class UserStorage {
    // SỬ DỤNG ConcurrentHashMap để Thread-safe
    private static Map<String, User> userMap = new ConcurrentHashMap<>();
    private static int currentMaxId = 0;

    // Giữ nguyên biến này để không làm hỏng code Controller cũ của bạn
    public static User currentUser = null;

    // ĐỒNG BỘ HÓA hàm tạo ID
    public static synchronized int generateNewId() {
        currentMaxId++;
        return currentMaxId;
    }

    public static void addUser(User user) {
        userMap.put(user.getUsername(), user);
    }

    public static boolean isUsernameExists(String username) {
        // ConcurrentHashMap cho phép kiểm tra trực tiếp an toàn
        return userMap.containsKey(username);
    }

    public static User getUser(String username) {
        return userMap.get(username);
    }

    public static User checkLogin(String username, String password) {
        User user = userMap.get(username);

        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return user;
        }
        return null;
    }

    public static void TruTien(Bidder bidder, Product product){
        bidder.ThanhToan(product);
    }
}