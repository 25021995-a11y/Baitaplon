package com.bidcycle.model;

import java.time.LocalDate;
import java.util.Map;

/**
 * MODEL: Người dùng có quyền quản trị hệ thống.
 * Quản lý: tài khoản người dùng, vật phẩm, tranh chấp, cấu hình hệ thống.
 */
public class Admin extends User {

    public Admin(int userId, String username, String password, boolean isActive) {
        super(userId, username, password, isActive);
    }

    public Admin(int userId, String username, String password, boolean isActive,
                 String email, String gender, LocalDate createdDate) {
        super(userId, username, password, isActive, email, gender, createdDate);
    }

    @Override
    public String getRoleName() {
        return "Admin";
    }

    // ─── Quản lý Tài khoản ─────────────────────────────────────

    /**
     * Duyệt kích hoạt tài khoản người dùng mới.
     */
    public boolean approveUserAccount(User user) {
        user.setActive(true);
        return true; // DB call được xử lý trong AdminDAO
    }

    /**
     * Cấm/vô hiệu hóa tài khoản.
     */
    public boolean banUserAccount(User user, String reason) {
        user.setActive(false);
        return true; // DB call được xử lý trong AdminDAO
    }

    /**
     * Kiểm tra duyệt tài khoản.
     */
    public boolean deactivateUser(User user) {
        user.setActive(false);
        return true;
    }

    // ─── Quản lý Vật phẩm ──────────────────────────────────────

    /**
     * Duyệt vật phẩm hiển thị trên hệ thống.
     */
    public boolean approveProduct(Product product) {
        return true; // DB logic trong AdminDAO.approveItem()
    }

    /**
     * Từ chối/ẩn vật phẩm.
     */
    public boolean rejectProduct(Product product, String reason) {
        return true; // DB logic trong AdminDAO.rejectItem()
    }

    /**
     * Buộc kết thúc phiên đấu giá.
     */
    public boolean forceEndAuction(Product product) {
        product.setFinished(true);
        return true; // DB logic trong AdminDAO.forceStopAuction()
    }

    // ─── Quản lý Cấu hình & Thống kê ────────────────────────────

    /**
     * Thiết lập phí giao dịch của hệ thống.
     */
    public boolean setSystemFee(double feePercent) {
        if (feePercent < 0 || feePercent > 100) return false;
        return true; // DB logic trong AdminDAO.setTransactionFee()
    }

    /**
     * Lấy báo cáo thống kê tổng hợp.
     */
    public Map<String, Object> generateSystemReport() {
        // Trả về map chứa toàn bộ thống kê hệ thống
        return null; // Chi tiết trong AdminDAO.generateReport()
    }
}
