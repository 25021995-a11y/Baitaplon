module com.baitaplon.baitaplon {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.controlsfx.controls;
    requires javafx.graphics;
    requires javafx.base;

    opens com.baitaplon.baitaplon to javafx.fxml;
    exports com.baitaplon.baitaplon;
}