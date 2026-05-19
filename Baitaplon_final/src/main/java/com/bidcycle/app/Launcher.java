package com.bidcycle.app;

import javafx.application.Application;

/**
 * APP: Launcher riêng biệt để tránh lỗi module-path khi chạy JAR.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
