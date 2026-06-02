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

    private DatabaseConnection() {}

    /**
     * Mở và trả về một Connection mới.
     * Caller chịu trách nhiệm đóng Connection (dùng try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy MySQL JDBC Driver. Kiểm tra pom.xml.", e);
        }
        return DriverManager.getConnection(URL, DB_USER, PASSWORD);
    }
}
