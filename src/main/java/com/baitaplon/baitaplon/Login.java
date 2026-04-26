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
import java.io.IOException;

public class Login {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;
    @FXML
    protected void onLoginButtonClick(ActionEvent event) {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        User authenticatedUser = UserDAO.checkLogin(user, pass);
        if (authenticatedUser != null) {
            UserStorage.currentUser =  authenticatedUser;
            statusLabel.setText("Đăng nhập thành công! Đang chuyển màn hình...");
            statusLabel.setTextFill(Color.GREEN);
            System.out.println("Xin chào " + authenticatedUser.getUsername());
            try{
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                FXMLLoader fxmlLoader;
                String windowTitle = "";
                if (authenticatedUser instanceof Seller) {
                    System.out.println("Đang load giao diện cho SELLER...");
                    fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("seller-view.fxml"));
                    windowTitle = "Kênh Người Bán - " + authenticatedUser.getUsername();}
                else {
                    System.out.println("Đang load giao diện cho BIDDER...");
                    fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
                    windowTitle = "Sàn Đấu Giá Trực Tuyến - " + authenticatedUser.getUsername();
                }
                Parent root = fxmlLoader.load();
                Scene auctionScene = new Scene(root);
                currentStage.setScene(auctionScene);
                currentStage.setTitle(windowTitle);
                currentStage.centerOnScreen();
            }
            catch (IOException e) {
                e.printStackTrace();
                statusLabel.setText("Lỗi hệ thống: Không thể load màn hình Đấu giá!");
            }
        }
        else {

            statusLabel.setText("Sai tên đăng nhập hoặc mật khẩu!");
            statusLabel.setTextFill(Color.RED);
            passwordField.clear();
        }
    }
    @FXML
    protected void onCreateButtonClick(ActionEvent event){
        try{
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("register.fxml"));
            Parent root = fxmlLoader.load();
            Scene auctionScene = new Scene(root);
            currentStage.setScene(auctionScene);
            currentStage.setTitle("REGISTRATION" );
            currentStage.centerOnScreen();
        }
        catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Lỗi hệ thống: Không thể load registration");
        }
    }
}
