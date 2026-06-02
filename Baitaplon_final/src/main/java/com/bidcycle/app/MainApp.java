package com.bidcycle.app;

import com.bidcycle.util.ViewNavigator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * APP: Điểm khởi động ứng dụng JavaFX.
 * Chỉ chịu trách nhiệm: khởi tạo Stage, load màn hình đầu tiên.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Đảm bảo các bảng và Admin mặc định tồn tại
        com.bidcycle.dao.AdminDAO.ensureSystemConfigTableExists();
        com.bidcycle.dao.UserDAO.ensureAdminExists();

        FXMLLoader loader = ViewNavigator.createLoader(ViewNavigator.LOGIN);
        Scene scene = new Scene(loader.load());

        stage.setTitle("BidCycle");
        stage.setScene(scene);

        // Icon ứng dụng
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/iconn.png"));
            stage.getIcons().add(icon);
        } catch (Exception ignored) {}

        stage.show();
    }
}
