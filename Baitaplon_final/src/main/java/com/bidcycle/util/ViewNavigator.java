package com.bidcycle.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * UTIL: Tiện ích điều hướng màn hình (Scene switching).
 * Tập trung toàn bộ logic load FXML vào một chỗ.
 * Controllers chỉ gọi ViewNavigator.navigateTo(...) thay vì lặp code FXMLLoader.
 *
 * Quy ước đặt tên FXML: tất cả nằm trong /com/bidcycle/view/
 */
public class ViewNavigator {

    // Tên các màn hình — tập trung ở đây để dễ bảo trì
    public static final String LOGIN        = "login.fxml";
    public static final String REGISTER     = "register.fxml";
    public static final String BIDDER_HOME  = "bidder-home.fxml";
    public static final String SELLER_HOME  = "seller-home.fxml";
    public static final String ACCOUNT      = "account.fxml";
    public static final String PAYMENT      = "payment.fxml";
    public static final String INVENTORY    = "inventory.fxml";
    public static final String PRODUCT_DETAIL = "product-detail.fxml";
    public static final String DASHBOARD    = "dashboard.fxml";
    public static final String ADMIN_PANEL  = "admin-panel.fxml";

    private static final String BASE_PATH = "/com.bidcycle.view/";

    private ViewNavigator() {}

    /**
     * Chuyển sang màn hình mới trên cùng Stage, trả về controller để cấu hình thêm.
     */
    public static <T> T navigateTo(Stage stage, String fxmlName, String title) throws IOException {
        FXMLLoader loader = createLoader(fxmlName);
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.centerOnScreen();
        return loader.getController();
    }

    /**
     * Chuyển màn hình không cần lấy controller.
     */
    public static void navigateTo(Stage stage, String fxmlName, String title, Runnable onLoad)
            throws IOException {
        FXMLLoader loader = createLoader(fxmlName);
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.centerOnScreen();
        if (onLoad != null) onLoad.run();
    }

    /**
     * Tạo FXMLLoader từ tên file.
     */
    public static FXMLLoader createLoader(String fxmlName) {
        return new FXMLLoader(ViewNavigator.class.getResource(BASE_PATH + fxmlName));
    }
}
