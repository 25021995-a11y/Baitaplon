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
public class Payment {
    @FXML
    private Label RemainMoney;
    //Hiển thị số dư hiện tại
    @FXML
    public void initialize(){
        User loggedInUser = UserStorage.currentUser;
        if(loggedInUser instanceof Bidder){
            RemainMoney.setText(String.valueOf(((Bidder) loggedInUser).getMoney()));
            RemainMoney.setTextFill(Color.GREEN);
        }
    }
    //Đã chỉnh lại phần Nạp tiền
    public void handleDeposit(double amount){
        User loggedInUser = UserStorage.currentUser;
        if(loggedInUser==null){
            System.out.println("Đăng nhập để nạp tiền");
            return;
        }
        if (loggedInUser instanceof Bidder){
            Bidder bidder=(Bidder) loggedInUser;
            bidder.deposit(amount);
            UserDAO.updateUserMoney(bidder.getUsername(), bidder.getMoney(), bidder.getVirMoney());
            System.out.println("Đang lưu tiền vào DB cho User ID: " + bidder.getUserId() + " | Số tiền mới: " + bidder.getMoney());

            boolean isSaved = UserDAO.updateUserMoney(bidder.getUsername(), bidder.getMoney(), bidder.getVirMoney());

            System.out.println("Kết quả lưu Database: " + (isSaved ? "✅ THÀNH CÔNG!" : "❌ THẤT BẠI!"));
            RemainMoney.setText(String.valueOf(bidder.getMoney()));
            RemainMoney.setTextFill(Color.GREEN);
        }
    }
    @FXML
    protected void FiftyButtonClick(ActionEvent event) {
        handleDeposit(50);
    }

    @FXML
    protected void HundredButtonClick(ActionEvent event) {
        handleDeposit(100);
    }

    @FXML
    protected void twoHundredButtonClick(ActionEvent event) {
        handleDeposit(200);
    }

    @FXML
    protected void fiveHundredButtonCLick(ActionEvent event) {
        handleDeposit(500);
    }

    @FXML
    protected void ThousandButtonClick(ActionEvent event) {
        handleDeposit(1000);
    }
    @FXML
    protected void BackToMainButtonClick(ActionEvent event){
        try{
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
            Parent root = fxmlLoader.load();
            Scene auctionScene = new Scene(root);
            currentStage.setScene(auctionScene);
            currentStage.centerOnScreen();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
