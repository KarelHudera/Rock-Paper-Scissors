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

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel, successLabel; // Labels to display error and success messages

    @FXML
    private StackPane errorBox, successBox, messageContainer; // Containers for error and success messages, and a general message container

    @FXML
    private Button loginButton;
    private Client gameClient; // Client instance for communicating with the game server

    private static final Logger logger = Logger.getLogger("ClientLogger");
    private Client client;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets up listeners to hide messages when input fields are typed into and initializes the game client.
     */
    @FXML
    private void initialize() {
        // Hide messages dynamically when typing
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> hideMessages());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> hideMessages());

        // Ensure the container starts empty
        messageContainer.setVisible(false);
        gameClient = new Client(logger);
    }

    /**
     * Handles the action when the login button is clicked.
     * Validates input, disables the login button, and initiates an asynchronous authentication task.
     */
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

        // Set the callback for when the login task succeeds
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

        // Set the callback for when the login task fails with an exception
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
                //TODO asi smazat
            }
        });

        // Spustíme úlohu na novém vlákně
        new Thread(loginTask).start();
    }

    /**
     * Initiates a short delay before navigating to the game screen, providing a smoother transition.
     */
    private void proceedToGameScreen() {
        PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
        delay.setOnFinished(event -> navigateToGameScreen());
        delay.play();
    }

    /**
     * Loads the game view FXML and displays it, replacing the login view.
     * Passes the game client instance to the new game controller.
     */
    private void navigateToGameScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/karel/hudera/rps/game-view.fxml"));
            Parent gameView = loader.load();

            GameController gameController = loader.getController();
            logger.info("LoginController: gameClient instance before passing to GameController: " + (this.gameClient != null ? "NOT NULL" : "NULL"));
            gameController.setClient(this.gameClient);

            Stage stage = (Stage) usernameField.getScene().getWindow(); // Nahraďte 'someButton' existujícím prvkem UI
            Scene scene = new Scene(gameView,  600, 400);
            stage.setScene(scene);
            stage.setTitle("Rock-Paper-Scissors Game"); // Nebo váš titul
            stage.show();

        } catch (IOException e) {
            logger.severe("Failed to load game-view.fxml: " + e.getMessage());
            e.printStackTrace();
            showMessage(errorBox, errorLabel, "Failed to load game. Please restart.");
            gameClient.closeConnection(); // Zavřeme spojení, pokud se nepodaří načíst hru
        }
    }

    /**
     * Displays a message in the specified message box and label, with a fade-in animation.
     * Hides other message boxes before displaying the new one.
     *
     * @param box The StackPane container for the message (e.g., errorBox, successBox).
     * @param label The Label inside the box to display the message text.
     * @param message The text message to display.
     */
    // Zobrazení a skrytí zpráv
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

    /**
     * Hides all message boxes (error and success) and the general message container.
     */
    private void hideMessages() {
        messageContainer.setVisible(false);
        errorBox.setVisible(false);
        successBox.setVisible(false);
    }

    /**
     * Applies a fade-in animation to the given StackPane.
     *
     * @param box The StackPane to apply the fade-in effect to.
     */
    private void fadeIn(StackPane box) { // Změněno z Label na StackPane
        box.setOpacity(0); // Začínáme s plně průhledným
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), box);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
}