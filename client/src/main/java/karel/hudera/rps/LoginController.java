package karel.hudera.rps;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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

    private static final Logger logger = Logger.getLogger("ClientLogger");
    private Client gameClient;

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

    @FXML
    private void initialize() {
        // Schovat placeholder text
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> hideMessages());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> hideMessages());

        messageContainer.setVisible(false);
        gameClient = new Client(logger);
    }

    /**
     * Ovláda co se stane po kliknutí na Login button v UI klienta.
     * Získá a ořeže uživatelské jméno a heslo uživatele.
     * Zablokuje možnost vykonat další akci v UI uživateli.
     * Pošle zprávu s údaji serveru a vyhodnotí příchozí zprávu od serveru.
     */
    @FXML
    private void onLoginButtonClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim(); // Prozatím se heslo na server neposílá

        // Username je povinné
        if (username.isEmpty()) {
            showMessage(errorBox, errorLabel, "Username cannot be empty.");
            return;
        }

        // Zablokujeme tlačítko, aby se zabránilo vícenásobnému kliknutí
        loginButton.setDisable(true);
        showMessage(successBox, successLabel, "Attempting to log in...");

        // Asynchronní volání autentizace
        Task<Boolean> loginTask = new Task<>() {
            String loginMessage = "";

            @Override
            protected Boolean call() {
                boolean success = gameClient.authenticate(username, password);
                if (!success) {
                    loginMessage = "Login failed. Invalid username or other error.";
                }
                return success;
            }
        };

        // Callback pro úspěšný login
        loginTask.setOnSucceeded(event -> {
            loginButton.setDisable(false);
            if (loginTask.getValue()) {
                showMessage(successBox, successLabel, "Login successful!");
                proceedToGameScreen();
            } else {
                showMessage(errorBox, errorLabel, loginTask.getMessage());
                gameClient.closeConnection();
            }
        });

        // Callback pro failed login
        loginTask.setOnFailed(event -> {
            loginButton.setDisable(false);
            logger.log(Level.SEVERE, "Login task failed: " + loginTask.getException().getMessage(), loginTask.getException());
            showMessage(errorBox, errorLabel, "An unexpected error occurred during login. See logs.");
            gameClient.closeConnection();
        });
        new Thread(loginTask).start();
    }

    /**
     * Přechod na herní obrazovku
     */
    private void proceedToGameScreen() {
        PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
        delay.setOnFinished(event -> navigateToGameScreen());
        delay.play();
    }

    /**
     * Načte a zobrazí FXML, nahradí předchozí zobrazené FXML.
     * Předává GameControlleru instanci klienta, tím zachová spojení.
     */
    private void navigateToGameScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/karel/hudera/rps/game-view.fxml"));
            Parent gameView = loader.load();

            GameController gameController = loader.getController();
            logger.info("LoginController: gameClient instance before passing to GameController: " + (this.gameClient != null ? "NOT NULL" : "NULL"));
            gameController.setClient(this.gameClient);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(gameView,  600, 400);
            stage.setScene(scene);
            stage.setTitle("Rock-Paper-Scissors Game");
            stage.show();

        } catch (IOException e) {
            logger.severe("Failed to load game-view.fxml: " + e.getMessage());
            e.printStackTrace();
            showMessage(errorBox, errorLabel, "Failed to load game. Please restart.");
            gameClient.closeConnection();
        }
    }

    /**
     * Zobrazí textovou zprávu v zadaném kontejneru a popisku.
     */
    private void showMessage(StackPane box, Label label, String message) {
        errorBox.setVisible(false);
        successBox.setVisible(false);
        messageContainer.setVisible(true);

        label.setText(message);
        box.setVisible(true);
        fadeIn(box);
    }

    /**
     * Skryje všechny kontejnery zpráv (chybové, úspěšné) a také hlavní kontejner zpráv.
     */
    private void hideMessages() {
        messageContainer.setVisible(false);
        errorBox.setVisible(false);
        successBox.setVisible(false);
    }

    /**
     * Animace StackPane
     */
    private void fadeIn(StackPane box) {
        box.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), box);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
}