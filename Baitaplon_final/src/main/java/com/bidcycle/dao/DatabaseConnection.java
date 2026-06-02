package com.bidcycle.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DAO (Data Access Object): Quản lý kết nối JDBC đến MySQL.
 * Tách riêng vào package dao — không nằm trong model hay controller.
 *
 * Cải tiến so với bản cũ:
 *  - Thông tin kết nối có thể đọc từ file cấu hình (mở rộng sau).
 *  - Không có main() test trong lớp này (vi phạm Single Responsibility).
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/biddingsystem";
    private static final String DB_USER  = "root";
    private static final String PASSWORD = "200807";

    // 1. Biến static duy nhất giữ instance của class
    private static DatabaseConnection instance;

    // 2. Private constructor để ngăn việc tạo đối tượng từ bên ngoài
    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 3. Phương thức public để lấy instance duy nhất
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Phương thức static để giữ tương thích ngược, giúp dự án không bị lỗi biên dịch.
     * Code cũ gọi DatabaseConnection.getConnection() sẽ đi qua Instance của Singleton.
     */
    public static Connection getConnection() throws SQLException {
        return getInstance().createConnection();
    }

    /**
     * Mở và trả về một Connection mới.
     */
    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, PASSWORD);
    }
}
