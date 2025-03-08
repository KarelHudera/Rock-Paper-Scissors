module karel.hudera.rps {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.logging;

    // shared module
    requires karel.hudera.rps.shared;

    opens karel.hudera.rps to javafx.fxml;
    exports karel.hudera.rps;
}