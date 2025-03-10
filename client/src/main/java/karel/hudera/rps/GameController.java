package karel.hudera.rps;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.logging.Logger;

public class GameController {

    private static final Logger logger = Logger.getLogger("GameLogger");

    @FXML
    private StackPane loadingPane;

    @FXML
    private HBox buttonsPane;

    @FXML
    private StackPane movePlayedPane;

    @FXML
    private Button rockButton, paperButton, scissorsButton;

    @FXML
    private Text selectedMoveText;

    @FXML
    private void initialize() {
        loadingPane.setVisible(false);
        buttonsPane.setVisible(true);
    }


    public void onOpponentConnected() {
        // Hide loading screen, show buttons
        loadingPane.setVisible(false);
        buttonsPane.setVisible(true);
    }

    public void onMovePlayed() {
        // Disable buttons, show move played screen
        rockButton.setDisable(true);
        paperButton.setDisable(true);
        scissorsButton.setDisable(true);
        movePlayedPane.setVisible(true);
    }

    @FXML
    private void onRockClick() {
        handleMove("ROCK");
    }

    @FXML
    private void onPaperClick() {
        handleMove("PAPER");
    }

    @FXML
    private void onScissorsClick() {
        handleMove("SCISSORS");
    }

    private void handleMove(String move) {
        logger.info("Player chose: " + move);

        // Display the selected move
        selectedMoveText.setText("You chose: " + move);

        // Hide buttons, show move played screen
        buttonsPane.setVisible(false);
        movePlayedPane.setVisible(true);
        // TODO: Send move to server and get the result
    }
}