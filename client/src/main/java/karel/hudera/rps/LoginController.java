package karel.hudera.rps;

import javafx.animation.FadeTransition;
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
    private Label errorLabel;

    @FXML
    private StackPane errorBox;

    @FXML
    private Button loginButton;

    private static final Logger logger = Logger.getLogger("ClientLogger");

    @FXML
    private void initialize() {
        // Hide error when user types
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> hideError());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> hideError());
    }

    @FXML
    private void onLoginButtonClick() throws IOException, ClassNotFoundException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password cannot be empty.");
            return;
        }

        Client client = new Client(logger);
        if (client.authenticate(username, password)) {
            navigateToGameScreen();
        } else {
            showError("Invalid username or password.");
        }
    }

    private void navigateToGameScreen() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("game-view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(fxmlLoader.load(),600,400));
            stage.setTitle("Rock-Paper-Scissors - Game");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setOpacity(0);
        errorBox.setVisible(true);

        // Smooth fade-in effect
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), errorLabel);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setOpacity(0);
        errorBox.setVisible(false);
    }
}