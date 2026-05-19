package com.bidcycle.model;

/**
 * MODEL: Quản lý phiên đăng nhập hiện tại (Session).
 */
public class UserSession {

    private static User currentUser = null;

    private UserSession() {}

    public static User getCurrentUser()       { return currentUser; }
    public static void setCurrentUser(User u) { currentUser = u; }
    public static void clearSession()         { currentUser = null; }
    public static boolean isLoggedIn()        { return currentUser != null; }
}
