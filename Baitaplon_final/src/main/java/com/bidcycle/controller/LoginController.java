package com.bidcycle.controller;

import com.bidcycle.dao.UserDAO;
import com.bidcycle.model.User;
import com.bidcycle.model.UserSession;
import com.bidcycle.util.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         statusLabel;

    @FXML
    protected void onLoginButtonClick(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!"); return;
        }

        User user = UserDAO.checkLogin(username, password);
        if (user == null) {
            showError("Sai tên đăng nhập hoặc mật khẩu!");
            passwordField.clear(); return;
        }

        UserSession.setCurrentUser(user);
        showSuccess("Đăng nhập thành công! Đang chuyển màn hình...");

        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            if (user instanceof com.bidcycle.model.Admin) {
                ViewNavigator.navigateTo(stage, ViewNavigator.ADMIN_PANEL,
                    "BidCycle - Quản trị hệ thống - " + user.getUsername());
            } else {
                ViewNavigator.navigateTo(stage, ViewNavigator.BIDDER_HOME,
                    "BidCycle - Trang chủ - " + user.getUsername());
            }
        } catch (IOException e) {
            showError("Lỗi hệ thống: Không thể tải màn hình chính!");
            e.printStackTrace();
        }
    }

    @FXML
    protected void onCreateButtonClick(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ViewNavigator.navigateTo(stage, ViewNavigator.REGISTER, "BidCycle - Đăng ký");
        } catch (IOException e) {
            showError("Lỗi hệ thống: Không thể tải màn hình đăng ký!");
            e.printStackTrace();
        }
    }

    private void showError(String m)   { statusLabel.setText(m); statusLabel.setTextFill(Color.RED); }
    private void showSuccess(String m) { statusLabel.setText(m); statusLabel.setTextFill(Color.GREEN); }
}
