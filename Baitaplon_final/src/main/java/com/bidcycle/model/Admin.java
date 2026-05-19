package com.bidcycle.model;

/**
 * MODEL: Người dùng có quyền quản trị hệ thống.
 */
public class Admin extends User {

    public Admin(int userId, String username, String password, boolean isActive) {
        super(userId, username, password, isActive);
    }

    @Override
    public String getRoleName() { return "Admin"; }

    public void deactivateUser(User user) {
        user.setActive(false);
    }
}
