package com.baitaplon.baitaplon;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/biddingsystem";
    private static final String USER = "root";
    private static final String PASSWORD = "200807";
    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println(" Đã kết nối thành công với database biddingsystem!");
        } catch (ClassNotFoundException e) {
            System.out.println("Lỗi: Chưa tải được thư viện MySQL (Hãy kiểm tra lại bước reload pom.xml)");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println(" Lỗi kết nối: Sai tên đăng nhập, mật khẩu hoặc MySQL chưa bật.");
            e.printStackTrace();
        }
        return connection;

        //testthu123
    }
    public static void main(String[] args) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
