module com.example.tank {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.almasb.fxgl.all;

    opens com.example.tank to javafx.fxml;
    exports com.example.tank;
}