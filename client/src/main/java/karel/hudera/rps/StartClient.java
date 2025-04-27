package karel.hudera.rps;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import karel.hudera.rps.client.Client;
import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.utils.Logging;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Entry point for starting the Rock-Paper-Scissors client.
 * <p>
 * This class initializes and connects the client to the server running on <b>localhost:9090</b>.
 * It also configures logging for better debugging and monitoring.
 * </p>
 *
 * @author Karel Hudera
 */
public class StartClient extends Application {

    static final Logger logger = Logger.getLogger("ClientLogger");

    public static void main(String[] args) {

        Logging.configureLogger(logger, Constants.LOG_FILE_C);

        Client client = new Client(logger);
        client.initialize("localhost", Constants.PORT);

        launch(); // This starts the JavaFX application
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(StartClient.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),600,400);
        stage.setTitle("Rock-Paper-Scissors");
        stage.setScene(scene);
        stage.show();
    }
}