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
 *      Toto je hlavní spouštěcí třída pro klientskou JavaFX aplikaci.
 *      Inicializuje logovací systém, tvoří instanci herního klienta
 *      a nastavuje a zobrazuje počáteční uživatelské rozhraní (přihlašovací obrazovku).
 */
public class StartClient extends Application {

    static final Logger logger = Logger.getLogger("ClientLogger");

    public static void main(String[] args) {

        Logging.configureLogger(logger, Constants.LOG_FILE_C);

        Client client = new Client(logger);

        launch();
    }

    /**
     * Přepsaná metoda z třídy Application.
     * Je zodpovědná za sestavení a zobrazení počátečního uživatelského rozhraní.
     * **/
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(StartClient.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),600,400);
        stage.setTitle("Rock-Paper-Scissors");
        stage.setScene(scene);
        stage.show();
    }

}