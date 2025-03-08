package karel.hudera.rps;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import java.util.logging.Logger;

public class GameController {

    private static final Logger logger = Logger.getLogger("GameLogger");

    @FXML
    private Button rockButton;

    @FXML
    private Button paperButton;

    @FXML
    private Button scissorsButton;

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
        // TODO: Send move to server and get the result
    }
}