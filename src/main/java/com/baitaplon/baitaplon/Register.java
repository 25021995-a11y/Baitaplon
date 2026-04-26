package com.baitaplon.baitaplon;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import java.io.IOException;
public class Register {
    @FXML
    private TextField regUsernameField;

    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private PasswordField regPasswordField;

    @FXML
    private PasswordField regConfirmPasswordField;

    @FXML
    private Label regStatusLabel;
    @FXML
    public void initialize() {
        if (roleComboBox != null) {
            roleComboBox.getItems().addAll("Người Mua (Bidder)", "Người Bán (Seller)");
            roleComboBox.getSelectionModel().selectFirst();
        }
    }
    @FXML

    protected void onRegisterSubmit(ActionEvent event) {
        String user = regUsernameField.getText();
        String pass = regPasswordField.getText();
        String confirmPass = regConfirmPasswordField.getText();
        if (user.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            regStatusLabel.setText("Vui lòng điền tên và mật khẩu");
            regStatusLabel.setTextFill(Color.RED);
            return;
        }
        if (!pass.equals(confirmPass)) {
            regStatusLabel.setText("Mật khẩu nhập lại không khớp!");
            regStatusLabel.setTextFill(Color.RED);
            return;
        }
        if (UserDAO.isUsernameExist(user)) {
            regStatusLabel.setText("Tên đăng nhập đã tồn tại! Vui lòng chọn tên khác.");
            regStatusLabel.setTextFill(Color.RED);
            return;
        }
        int newId = UserStorage.generateNewId();
        String selectedRole = roleComboBox.getValue();
        String dbRole = "BIDDER";
        if ("Người Bán (Seller)".equals(selectedRole)) {
            dbRole = "SELLER";
        }
        boolean isSuccess = UserDAO.registerNewUser(user, pass, dbRole);
        if (isSuccess) {
            System.out.println("Đã tạo tài khoản " + dbRole + " thành công: " + user );
            regStatusLabel.setText("Đã tạo tài khoản thành công " + user);
            regStatusLabel.setTextFill(Color.GREEN);
            regUsernameField.clear();
            regPasswordField.clear();
            regConfirmPasswordField.clear();}
        else {
            regStatusLabel.setText("Lỗi kết nối cơ sở dữ liệu! Không thể tạo tài khoản.");
            regStatusLabel.setTextFill(Color.RED);
        }

    }

    @FXML
    protected void onBackToLogin(ActionEvent event){
        try{
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
            Parent root = fxmlLoader.load();
            Scene auctionScene = new Scene(root);
            currentStage.setScene(auctionScene);
            currentStage.setTitle("LOGIN" );
            currentStage.centerOnScreen();
        }
        catch (IOException e) {
            regPasswordField.setText("Lỗi hệ thống: Không thể load registration");
        }
    }
}
