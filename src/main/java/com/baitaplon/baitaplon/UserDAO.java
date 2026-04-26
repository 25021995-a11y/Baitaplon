package com.baitaplon.baitaplon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
public class UserDAO {
    public static boolean isUsernameExist(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Nếu có kết quả trả về -> Tên này đã có người dùng!

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static User checkLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Truyền tham số vào câu SQL
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            // Thực thi truy vấn
            ResultSet rs = pstmt.executeQuery();

            // Nếu rs.next() là true -> Tức là tìm thấy đúng 1 dòng khớp tài khoản & mật khẩu
            if (rs.next()) {
                int id = rs.getInt("id");
                boolean isActive = rs.getBoolean("is_active");
                String role = rs.getString("role");
                double currentMoney = rs.getDouble("money");
                double currentVirMoney = rs.getDouble("vir_money");
                // Khôi phục lại đúng Object tùy theo Role
                if ("SELLER".equals(role)) {
                    Seller seller = new Seller(id, username, password, isActive);
                    seller.setMoney(currentMoney);
                    return seller;
                }
                else if ("BIDDER".equals(role)) {
                    Bidder bidder = new Bidder(id, username, password, isActive);
                    bidder.setMoney(currentMoney);
                    bidder.setVirMoney(currentVirMoney);
                    return bidder;
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi truy vấn đăng nhập: ");
            e.printStackTrace();
        }

        // Trả về null nếu không tìm thấy tài khoản (sai user/pass)
        return null;
    }
    public static boolean updateUserMoney(String username, double newMoney, double newVirMoney) {
        // Lệnh UPDATE để tìm đúng người (WHERE id = ?) và sửa lại 2 cột tiền
        String sql = "UPDATE users SET money = ?, vir_money = ? WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newMoney);
            pstmt.setDouble(2, newVirMoney);
            pstmt.setString(3, username); // Lấy ID của người dùng để cập nhật đúng người

            // Thực thi lệnh cập nhật
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Trả về true nếu cập nhật thành công

        } catch (SQLException e) {
            System.out.println("Lỗi khi cập nhật tiền vào DB!");
            e.printStackTrace();
            return false;
        }
    }
    public static boolean registerNewUser(String username, String password, String role) {

        if (isUsernameExist(username)) {
            System.out.println(" Lỗi: Tên đăng nhập '" + username + "' đã tồn tại!");
            return false;
        }

        String sql = "INSERT INTO users (username, password, is_active, role, money, vir_money) VALUES (?, ?, TRUE, ?, 0.0, 0.0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role); // "BIDDER" hoặc "SELLER"

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; //true

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
