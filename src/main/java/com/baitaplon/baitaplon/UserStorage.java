package com.baitaplon.baitaplon;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
public class UserStorage {
    private static Map<String, User> userMap = new HashMap<>();
    private static int currentMaxId = 0;
    public static int generateNewId() {
        currentMaxId++;
        return currentMaxId;
    }

    public static User currentUser = null;

    public static void addUser(User user) {
        userMap.put(user.getUsername(), user);
    }
    public static boolean isUsernameExists(String username) {

        for (User user : userMap.values()) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
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
    public static void TruTien(Bidder bidder,Product product){
        bidder.ThanhToan(product);
    }
}
