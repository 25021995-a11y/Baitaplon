package com.bidcycle.controller;

import com.bidcycle.dao.UserDAO;
import com.bidcycle.util.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * CONTROLLER: Đăng ký tài khoản — không phân biệt vai trò.
 */
public class RegisterController {

    @FXML private TextField     regUsernameField;
    @FXML private TextField     regEmailField;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private PasswordField regPasswordField;
    @FXML private PasswordField regConfirmPasswordField;
    @FXML private Label         regStatusLabel;

    @FXML
    public void initialize() {
        genderComboBox.getItems().addAll("Nam", "Nữ", "Khác");
        genderComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    protected void onRegisterSubmit(ActionEvent event) {
        String username    = regUsernameField.getText().trim();
        String password    = regPasswordField.getText();
        String confirmPass = regConfirmPasswordField.getText();
        String email       = regEmailField != null ? regEmailField.getText().trim() : "";
        String gender      = genderComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin!"); return;
        }
        if (!password.equals(confirmPass)) {
            showError("Mật khẩu nhập lại không khớp!"); return;
        }
        if (!email.isEmpty() && !isValidEmail(email)) {
            showError("Email không hợp lệ! (vd: user@email.com)"); return;
        }
        String pwdError = getPasswordStrengthError(password);
        if (pwdError != null) { showError(pwdError); return; }
        if (UserDAO.isUsernameExist(username)) {
            showError("Tên đăng nhập đã tồn tại!"); return;
        }

        boolean success = UserDAO.registerNewUser(username, password, email, gender);
        if (success) {
            showSuccess("✅ Tạo tài khoản thành công: " + username);
            clearForm();
        } else {
            showError("❌ Lỗi kết nối cơ sở dữ liệu!");
        }
    }

    @FXML
    protected void onBackToLogin(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ViewNavigator.navigateTo(stage, ViewNavigator.LOGIN, "BidCycle - Đăng nhập");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private String getPasswordStrengthError(String p) {
        if (p.length() < 8)                  return "Mật khẩu phải có ít nhất 8 ký tự!";
        if (!p.matches(".*[A-Z].*"))          return "Mật khẩu phải có ít nhất 1 chữ hoa!";
        if (!p.matches(".*[a-z].*"))          return "Mật khẩu phải có ít nhất 1 chữ thường!";
        if (!p.matches(".*[0-9].*"))          return "Mật khẩu phải có ít nhất 1 chữ số!";
        if (!p.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"))
                                              return "Mật khẩu phải có ít nhất 1 ký tự đặc biệt!";
        return null;
    }

    private void showError(String msg)   { regStatusLabel.setText(msg); regStatusLabel.setTextFill(Color.RED); }
    private void showSuccess(String msg) { regStatusLabel.setText(msg); regStatusLabel.setTextFill(Color.GREEN); }
    private void clearForm() {
        regUsernameField.clear(); regPasswordField.clear(); regConfirmPasswordField.clear();
        if (regEmailField != null) regEmailField.clear();
    }
}
