module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires com.google.gson;

    opens org.example.demo to javafx.fxml;
    exports org.example.demo;
}