package com.bidcycle.dao;
import java.sql.Connection;
import java.sql.DriverManager;

public class TestCloud {
    public static void main(String[] args) {
        // Thay IP nếu bạn đã lấy được IP ở Bước 2, hoặc giữ nguyên URL này
        String url = "jdbc:mysql://zephyr.proxy.rlwy.net:56046/railway?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "rBPtxSGQiirmJeAOBiYgzLqAIQPmcgAm";

        System.out.println("Đang thử kết nối lên Clever Cloud...");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("🎉 KẾT NỐI THÀNH CÔNG! DB trên Cloud hoạt động hoàn hảo!");
            conn.close();
        } catch (Exception e) {
            System.err.println("❌ KẾT NỐI THẤT BẠI. Lỗi chi tiết:");
            e.printStackTrace();
        }
    }
}