package karel.hudera.rps;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import karel.hudera.rps.client.Client;

import java.io.IOException;
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

    private static final Logger logger = Logger.getLogger("ClientLogger");

    @FXML
    private void initialize() {
        // Hide messages dynamically when typing
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> hideMessages());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> hideMessages());

        // Ensure the container starts empty
        messageContainer.setVisible(false);
    }

    @FXML
    private void onLoginButtonClick() throws IOException, ClassNotFoundException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage(errorBox, errorLabel, "Username and password cannot be empty.");
            return;
        }

        Client client = new Client(logger);
        if (client.authenticate(username, password)) {
            showMessage(successBox, successLabel, "Login successful! Redirecting...");
            proceedToGameScreen();
        } else {
            showMessage(errorBox, errorLabel, "Invalid username or password.");
        }
    }

    private void proceedToGameScreen() {
        PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
        delay.setOnFinished(event -> navigateToGameScreen());
        delay.play();
    }

    private void navigateToGameScreen() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game-view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(fxmlLoader.load(), 600, 400));
            stage.setTitle("Rock-Paper-Scissors - Game");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showMessage(StackPane box, Label label, String message) {
        // Hide all messages first
        errorBox.setVisible(false);
        successBox.setVisible(false);
        messageContainer.setVisible(true);

        label.setText(message);
        box.setVisible(true);
        fadeIn(label);
    }

    private void hideMessages() {
        messageContainer.setVisible(false);
        errorBox.setVisible(false);
        successBox.setVisible(false);
    }

    private void fadeIn(Label label) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), label);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
}