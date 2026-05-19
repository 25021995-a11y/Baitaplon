module com.bidcycle {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.controlsfx.controls;

    // Mở các package Controller cho FXML (JavaFX reflection)
    opens com.bidcycle.controller to javafx.fxml;
    opens com.bidcycle.app to javafx.graphics,javafx.fxml;


    // Export các package public
    exports com.bidcycle.app;
    exports com.bidcycle.model;
    exports com.bidcycle.controller;
    exports com.bidcycle.dao;
    exports com.bidcycle.util;
}