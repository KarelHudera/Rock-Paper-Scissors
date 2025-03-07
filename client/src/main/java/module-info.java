module karel.hudera.rps.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.logging;

    requires karel.hudera.rps.shared;

    opens karel.hudera.rps.client to javafx.fxml;
    exports karel.hudera.rps.client;
}