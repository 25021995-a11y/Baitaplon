package com.baitaplon.baitaplon;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import javafx.scene.control.CheckBox;
public class HelloController {

    @FXML
    private CheckBox chkAutoBid;
    @FXML
    private TableView<Product> auctionTable;
    @FXML
    private TextField bidInput;
    @FXML
    private TableColumn<Product, String> productColumn;

    @FXML
    private TableColumn<Product, Double> priceColumn;
    @FXML
    private TableColumn<Product,String> timeColumn;
    @FXML
    private TableColumn<Product,String> topBidColumn;
    @FXML
    private Button bidButton;
    @FXML
    private Button inventory;
    @FXML
    private Button refreshButton;
    @FXML
    private Button PayButton;
    @FXML
    private Label userName;
    @FXML
    private Label Wallet;
    @FXML private TextField txtStepPrice;
    @FXML private Label statusLabel;
    //Phân ra hai vai trò cụ thể cho seller và bidder
    public void initialize(){
        User loggedInUser = UserStorage.currentUser;
        if(loggedInUser !=null){
            userName.setText(loggedInUser.getUsername());
            userName.setTextFill(Color.GREEN);
            if(loggedInUser instanceof Seller){
                Seller seller=(Seller) loggedInUser;
                Wallet.setText(String.valueOf(seller.getMoney()));
            } else if (loggedInUser instanceof Bidder) {
                Bidder bidder=(Bidder) loggedInUser;
                Wallet.setText(String.valueOf(bidder.getMoney()));
            }
        }
        productColumn.setCellValueFactory(sanpham -> new SimpleStringProperty(sanpham.getValue().getName()));
        priceColumn.setCellValueFactory(gia->new SimpleObjectProperty<>(gia.getValue().getPrice()));
        timeColumn.setCellValueFactory(time-> new SimpleStringProperty(time.getValue().getTimeDisplay()));
        topBidColumn.setCellValueFactory(cellData->{
            Bidder highestBidder = cellData.getValue().getHighestBidder();
            if (highestBidder != null){
                return new SimpleObjectProperty<>(highestBidder.getUsername());
            }
            else{
                return new SimpleObjectProperty<>("Chưa có");
            }
        });

        Seller seller1=new Seller(1,"Tuan","123456789",true);
        ObservableList<Product> ListItem = FXCollections.observableArrayList();
        seller1.pushItem("braclet",100,1);
        seller1.pushItem("necklet",200,1);
        ListItem.addAll(ProductStorage.getAllProducts().values());
        auctionTable.setItems(ListItem);
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event-> {
            for(Product items:ListItem){
                items.checkAndProcessEnd();
            }
            auctionTable.refresh();
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }
    @FXML
    protected void onBidButtonClick(ActionEvent event) {
        User loggedInUser = UserStorage.currentUser;
        if (loggedInUser == null) {
            System.out.println("Vui lòng đăng nhập để đấu giá!");
            return;
        }
        if (!(loggedInUser instanceof Bidder)){
            System.out.println("Chỉ Bidder mới được tham gia đấu giá");
        }

        System.out.println("Bạn vừa bấm nút Tham gia đấu giá!");
        Product selecP=auctionTable.getSelectionModel().getSelectedItem();
        if(selecP==null){
            System.out.println("khong chon san pham nao");
            return;
        }
        if (selecP.getFinished()==true) {
            System.out.println("Sản phẩm này đã kết thúc đấu giá!");
            return;
        }
        String bidInputText = bidInput.getText();
        if (bidInputText == null || bidInputText.trim().isEmpty()) {
            System.out.println("Bạn chưa nhập số tiền muốn trả ");
            return;
        }
        try{
            double bidPrice=Double.parseDouble(bidInputText);
            boolean isAutoBid = chkAutoBid.isSelected();
            double stepPrice = 0.0;
            if (isAutoBid) {
                if (txtStepPrice.getText().trim().isEmpty()) {
                    statusLabel.setText("Vui lòng nhập bước giá cho Auto-Bid!");
                    statusLabel.setTextFill(Color.RED);
                    return;
                }
                stepPrice = Double.parseDouble(txtStepPrice.getText());
            }
            Bidder bidder=(Bidder) loggedInUser;
            if (bidder.getVirMoney() >= bidPrice) {
                String resultMsg = selecP.processBid(bidder, bidPrice, isAutoBid, stepPrice);
                statusLabel.setText(resultMsg);
                if (!resultMsg.contains("Lỗi")) {
                    bidder.virMoneyPay(bidPrice);
                    int snipeWindow = 30;
                    int timeExtension = 60;
                    if (selecP.getTimeRemaining() <= snipeWindow){
                        selecP.addExtraTime(timeExtension);
                    }
                }
                bidInput.clear();
                if (txtStepPrice != null) txtStepPrice.clear();

                Wallet.setText(String.valueOf(bidder.getMoney()));
                Wallet.setTextFill(Color.GREEN);
                auctionTable.refresh();

            } else {
                System.out.println("Số dư không đủ để đặt mức giá này!");
            }
        }
        catch (NumberFormatException e){
            System.err.println("Chỉ nhập số");
        }
    }
    @FXML
    protected void onRefreshButtonClick(ActionEvent event) {
        System.out.println("Bạn vừa bấm nút Làm mới danh sách");
        if (auctionTable != null) {
            User loggedInUser = UserStorage.currentUser;
            if(loggedInUser instanceof Bidder){
                Bidder bidder=(Bidder) loggedInUser;
                Wallet.setText(String.valueOf(bidder.getMoney()));
                Wallet.setTextFill(Color.GREEN);
            }
            else if (loggedInUser instanceof Seller) {
                Seller seller=(Seller) loggedInUser;
                Wallet.setText(String.valueOf(seller.getMoney()));
                Wallet.setTextFill(Color.GREEN);
            }
            auctionTable.refresh();
            System.out.println("Đã làm mới bảng dữ liệu");
        }
        else{
            System.out.println("bảng rỗng");
        }
    }
    @FXML
    protected void onPayButtonCLick(ActionEvent event){
        try{
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("PayInterface.fxml"));
            Parent root = fxmlLoader.load();
            Scene auctionScene = new Scene(root);
            currentStage.setScene(auctionScene);
            currentStage.centerOnScreen();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void inventoryButtonClick(ActionEvent event){
        try{
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Inventory.fxml"));
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