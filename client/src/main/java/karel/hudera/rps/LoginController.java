package karel.hudera.rps;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import karel.hudera.rps.client.Client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel, successLabel;

    @FXML
    private StackPane errorBox, successBox, messageContainer;

    @FXML
    private Button loginButton;
    private Client gameClient;

    private static final Logger logger = Logger.getLogger("ClientLogger");

    @FXML
    private void initialize() {
        // Hide messages dynamically when typing
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> hideMessages());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> hideMessages());

        // Ensure the container starts empty
        messageContainer.setVisible(false);
        gameClient = new Client(logger);
    }
    @FXML
    private void onLoginButtonClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim(); // Prozatím se heslo na server neposílá

        if (username.isEmpty()) { // Heslo není povinné pro LoginRequest
            showMessage(errorBox, errorLabel, "Username cannot be empty.");
            return;
        }

        // Zablokujeme tlačítko, aby se zabránilo vícenásobnému kliknutí
        loginButton.setDisable(true);
        showMessage(successBox, successLabel, "Attempting to log in..."); // Zpráva pro uživatele

        // Vytvoříme Task pro asynchronní volání autentizace
        Task<Boolean> loginTask = new Task<>() {
            String loginMessage = ""; // Pro zprávu ze serveru

            @Override
            protected Boolean call() throws Exception {
                // Tato část se spustí na pozadí (jiném vlákně)
                boolean success = gameClient.authenticate(username, password);
                if (!success) {
                    // hardcoded
                    loginMessage = "Login failed. Invalid username or other error.";
                }
                return success;
            }
        };

        loginTask.setOnSucceeded(event -> {
            // Tato část se spustí na JavaFX Application Thread
            loginButton.setDisable(false); // Povolíme tlačítko zpět
            if (loginTask.getValue()) { // .getValue() vrátí výsledek z call()
                showMessage(successBox, successLabel, "Login successful!");
                proceedToGameScreen();
            } else {
                showMessage(errorBox, errorLabel, loginTask.getMessage()); // Zobrazí zprávu z úlohy
                gameClient.closeConnection(); // Důležité: zavřeme spojení, pokud login selhal
            }
        });

        loginTask.setOnFailed(event -> {
            // Tato část se spustí na JavaFX Application Thread, pokud Task selže s výjimkou
            loginButton.setDisable(false); // Povolíme tlačítko zpět
            logger.log(Level.SEVERE, "Login task failed: " + loginTask.getException().getMessage(), loginTask.getException());
            showMessage(errorBox, errorLabel, "An unexpected error occurred during login. See logs.");
            gameClient.closeConnection(); // Zavřeme spojení
        });

        // Nastavíme zprávu, kterou úloha použije, pokud selže
        loginTask.messageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                // Tato zpráva se aktualizuje z call() metody, pokud je nastavena
                // (pro naši implementaci loginu ji možná nebudeme přímo používat,
                // ale je to dobrý vzor pro složitější Tasks)
            }
        });

        // Spustíme úlohu na novém vlákně
        new Thread(loginTask).start();
    }

    private void proceedToGameScreen() {
        PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
        delay.setOnFinished(event -> navigateToGameScreen());
        delay.play();
    }

    private void navigateToGameScreen() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game-view.fxml"));
            // Předáme instanci klienta dál do nového kontroleru
            GameController gameController = new GameController(gameClient);
            fxmlLoader.setController(gameController);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(fxmlLoader.load(), 600, 400));
            stage.setTitle("Rock-Paper-Scissors - Game");
            stage.show(); // Zobrazíme nové okno

        } catch (IOException e) {
            logger.severe("Failed to load game-view.fxml: " + e.getMessage());
            e.printStackTrace();
            showMessage(errorBox, errorLabel, "Failed to load game. Please restart.");
            gameClient.closeConnection(); // Zavřeme spojení, pokud se nepodaří načíst hru
        }
    }

    // Metody pro zobrazení a skrytí zpráv
    private void showMessage(StackPane box, Label label, String message) {
        // Skrýt všechny zprávy nejprve
        errorBox.setVisible(false);
        successBox.setVisible(false);
        messageContainer.setVisible(true); // Zajistí, že kontejner je viditelný

        label.setText(message);
        box.setVisible(true);
        // Fade-in efekt na celém boxu (ne jen labelu pro lepší vizuál)
        fadeIn(box);
    }

    private void hideMessages() {
        messageContainer.setVisible(false);
        errorBox.setVisible(false);
        successBox.setVisible(false);
    }

    private void fadeIn(StackPane box) { // Změněno z Label na StackPane
        box.setOpacity(0); // Začínáme s plně průhledným
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), box);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
}