package com.bidcycle.dao;

import com.bidcycle.model.User;

import java.sql.*;
import java.time.LocalDate;

/**
 * DAO: Toàn bộ thao tác CRUD với bảng users trong database.
 */
public class UserDAO {

    private UserDAO() {}

    public static boolean isUsernameExist(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            return pstmt.executeQuery().next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Xác thực đăng nhập, trả về User nếu hợp lệ.
     */
    public static User checkLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapResultSetToUser(rs, username, password);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static boolean updateUserInfo(String username, String email, String gender, String newPassword) {
        String sql = (newPassword != null && !newPassword.isEmpty())
            ? "UPDATE users SET email = ?, gender = ?, password = ? WHERE username = ?"
            : "UPDATE users SET email = ?, gender = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, gender);
            if (newPassword != null && !newPassword.isEmpty()) {
                pstmt.setString(3, newPassword);
                pstmt.setString(4, username);
            } else {
                pstmt.setString(3, username);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Cập nhật số dư tiền thật và tiền khả dụng.
     */
    public static boolean updateUserMoney(String username, double newMoney, double newVirMoney) {
        String sql = "UPDATE users SET money = ?, vir_money = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newMoney);
            pstmt.setDouble(2, newVirMoney);
            pstmt.setString(3, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Đăng ký người dùng mới — không có role, tất cả đều là "USER".
     */
    public static boolean registerNewUser(String username, String password,
                                          String email, String gender) {
        if (isUsernameExist(username)) return false;
        String sql = "INSERT INTO users "
                   + "(username, password, is_active, role, money, vir_money, email, gender, created_date) "
                   + "VALUES (?, ?, TRUE, 'BIDDER', 0.0, 0.0, ?, ?, CURDATE())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, gender);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Fallback nếu DB cũ thiếu cột email/gender
            return registerNewUserBasic(username, password);
        }
    }

    private static boolean registerNewUserBasic(String username, String password) {
        String sql = "INSERT INTO users (username, password, is_active, role, money, vir_money) "
                   + "VALUES (?, ?, TRUE, 'BIDDER', 0.0, 0.0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── Mapping ───────────────────────────────────────────────

    private static User mapResultSetToUser(ResultSet rs, String username, String password)
            throws SQLException {
        int id        = rs.getInt("id");
        boolean isAct = rs.getBoolean("is_active");
        double money  = rs.getDouble("money");
        double virMon = rs.getDouble("vir_money");
        String email  = safeGetString(rs, "email",  "");
        String gender = safeGetString(rs, "gender", "Khác");
        LocalDate created = safeGetDate(rs, "created_date");

        User user = new User(id, username, password, isAct, email, gender, created);
        user.setMoney(money);
        user.setVirMoney(virMon);
        return user;
    }

    private static String safeGetString(ResultSet rs, String col, String def) {
        try { String v = rs.getString(col); return v != null ? v : def; }
        catch (Exception e) { return def; }
    }

    private static LocalDate safeGetDate(ResultSet rs, String col) {
        try { Date d = rs.getDate(col); return d != null ? d.toLocalDate() : LocalDate.now(); }
        catch (Exception e) { return LocalDate.now(); }
    }
    public static User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(
                            rs.getStatement().getResultSet(),
                            rs.getString("username"),
                            rs.getString("role")
                    );
                }
            } // Đóng try-with-resources của ResultSet
        } catch (SQLException e) { // Khối catch giờ đã liên kết chuẩn với try ở trên
            e.printStackTrace();
        }
        return null;
    }
}
