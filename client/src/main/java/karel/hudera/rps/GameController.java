package karel.hudera.rps;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import karel.hudera.rps.client.Client;
import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.game.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;


public class GameController implements Initializable {

    private static final Logger logger = Logger.getLogger("ClientLogger");

    private ExecutorService executorService; //pro asynchronní čtení zpráv
    @FXML private Label statusMessageLabel;
    @FXML private Label yourUsernameLabel;
    @FXML private Label opponentUsernameLabel;
    @FXML private Label yourScoreLabel;
    @FXML private Label opponentScoreLabel;
    @FXML private Label roundResultLabel;
    @FXML private Label waitingForOpponentMoveLabel; // Nový label

    @FXML
    private VBox gameContentContainer;
    @FXML
    private VBox waitingOverlayContainer;

    @FXML private Button rockButton;
    @FXML private Button paperButton;
    @FXML private Button scissorsButton;
    @FXML private Button disconnectButton;
    //logování událostí hry
    private Client client;
    private String loggedInUsername;// Reference na tvou Client instanci
    private String opponentUsername;

    // Skóre
    private int yourScore = 0;
    private int opponentScore = 0;

    public GameController() {}

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets initial UI visibility and starts the thread for listening to server messages.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        gameContentContainer.setVisible(true);
        waitingOverlayContainer.setVisible(false);

        yourUsernameLabel.setText(loggedInUsername);
        statusMessageLabel.setText(Constants.WAITING_FOR_OPPONENT); // "Waiting for opponent..."
        opponentUsernameLabel.setText("...");
        yourScoreLabel.setText("0");
        opponentScoreLabel.setText("0");
        roundResultLabel.setText("");
        waitingForOpponentMoveLabel.setText("Pick your move!"); // Výchozí text

        setMoveButtonsEnabled(false); // Tlačítka jsou na začátku zakázána
    }

    private void startMessageListener() {
        logger.info("GameController: Entering startMessageListener. Value of 'this.client' is: " + (this.client != null ? "NOT NULL" : "NULL"));


        if (this.client != null) {
            logger.info("GameController: CHECKING client.isConnected() BEFORE THREAD START: " + this.client.isConnected());
        } else {
            logger.severe("GameController: client is NULL when trying to check isConnected() before thread start. This is unexpected.");
            return; // Zastavit, pokud je client null
        }


        new Thread(() -> {
            logger.info("GameController: ClientMessageListener thread started. Value of 'client' (captured by lambda) is: " + (client != null ? "NOT NULL" : "NULL"));
            try {
                // Tento řádek už zde být nemusí, protože jsme ho zkontrolovali výše
                // logger.info("GameController: BEFORE WHILE LOOP, client.isConnected() returns: " + this.client.isConnected());
                while (client.isConnected()) {
                    GameMessage message = client.readServerMessage();
                    if (message != null) {
                        Platform.runLater(() -> handleIncomingMessage(message));
                    }
                    Thread.sleep(50);
                }
            } catch (IOException e) {
                logger.severe("Connection lost: " + e.getMessage());
                Platform.runLater(() -> {
                    statusMessageLabel.setText("Connection lost. Please restart.");
                    setMoveButtonsEnabled(false);
                    if (disconnectButton != null) disconnectButton.setDisable(true);
                });
            } catch (ClassNotFoundException e) {
                logger.severe("Received unknown object from server: " + e.getMessage());
                Platform.runLater(() -> statusMessageLabel.setText("Communication error."));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warning("Message listener interrupted.");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected error in ClientMessageListener thread: " + e.getMessage(), e);
                Platform.runLater(() -> {
                    statusMessageLabel.setText("Critical error. Please restart.");
                    setMoveButtonsEnabled(false);
                    if (disconnectButton != null) disconnectButton.setDisable(true);
                });
            }finally {
                logger.info("Client message listener stopped.");
            }
        }, "ClientMessageListener").start();
    }

    private void handleIncomingMessage(GameMessage message) {
        logger.info("Received message in GameController: " + message.getClass().getSimpleName() + " - " + message.toString());

        if (message instanceof GameStart) {
            GameStart gameStartMessage = (GameStart) message;
            opponentUsername = gameStartMessage.getOpponentUsername();

            statusMessageLabel.setText("Game Started!");
            opponentUsernameLabel.setText(opponentUsername);
            yourScore = 0;
            opponentScore = 0;
            yourScoreLabel.setText("0");
            opponentScoreLabel.setText("0");
            roundResultLabel.setText("");
            waitingForOpponentMoveLabel.setText("Pick your move!");
            setMoveButtonsEnabled(true); // Povol tlačítka pro tah

        } else if (message instanceof RoundResult) {
            RoundResult roundResult = (RoundResult) message;

            // Aktualizace skóre
            yourScore = roundResult.getPlayer1Score(); // Předpokládám, že player1Score je tvé
            opponentScore = roundResult.getPlayer2Score(); // Předpokládám, že player2Score je soupeře
            yourScoreLabel.setText(String.valueOf(yourScore));
            opponentScoreLabel.setText(String.valueOf(opponentScore));

            // Zobrazení výsledku kola
            String resultText = "";
            if (roundResult.getRoundResult() == Result.WIN) {
                resultText = "YOU WIN this round!";
                roundResultLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else if (roundResult.getRoundResult() == Result.LOSE) {
                resultText = "YOU LOSE this round!";
                roundResultLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            } else { // DRAW
                resultText = "IT'S A DRAW!";
                roundResultLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            }
            roundResultLabel.setText(resultText);

            // Zobrazení tahů
            waitingForOpponentMoveLabel.setText(String.format("You chose: %s, Opponent chose: %s",
                    roundResult.getYourMove().name(), roundResult.getOpponentMove().name()));

            statusMessageLabel.setText("Round finished. Pick your next move!");
            setMoveButtonsEnabled(true); // Povol tlačítka pro další tah
/**
        } else if (message instanceof GameOver) {
            GameOver gameOverMessage = (GameOver) message;
            statusMessageLabel.setText("GAME OVER! " + gameOverMessage.getReason());
            setMoveButtonsEnabled(false); // Zakázání tlačítek tahů
            disconnectButton.setDisable(false); // Povolí jen disconnect
            waitingForOpponentMoveLabel.setText(""); // Skryj text o tahu

            logger.info("Game Over: " + gameOverMessage.getReason());

        } else if (message instanceof PingMessage) {
            logger.info("Received PING message from server.");
 **/
        } else {
            logger.warning("Unhandled message type received: " + message.getClass().getSimpleName());
        }
    }

    @FXML
    private void handleMove(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String moveName = ""; // Bude obsahovat ROCK, PAPER nebo SCISSORS
        Move move = null;
        // Určení tahu podle ID tlačítka
        if (clickedButton == rockButton) {
            moveName = "ROCK";
            move = Move.ROCK;
        } else if (clickedButton == paperButton) {
            moveName = "PAPER";
            move = Move.PAPER;
        } else if (clickedButton == scissorsButton) {
            moveName = "SCISSORS";
            move = Move.SCISSORS;
        }

        try {
            // GameMove -> enum (ROCK, PAPER, SCISSORS)
            GameMove gameMove = new GameMove(move);
            client.sendToServer(gameMove);
            statusMessageLabel.setText("You chose " + moveName + ".");
            waitingForOpponentMoveLabel.setText("Waiting for opponent's move...");
            setMoveButtonsEnabled(false); // Zakázat tlačítka po odeslání tahu
            roundResultLabel.setText(""); // Vyčisti předchozí výsledek

            // změna UI
            gameContentContainer.setVisible(false);
            waitingOverlayContainer.setVisible(true);
        } catch (IOException e) {
            logger.severe("Failed to send move: " + e.getMessage());
            statusMessageLabel.setText("Error sending move. Connection lost?");
            setMoveButtonsEnabled(false);
        }
    }

    @FXML
    private void handleDisconnect(ActionEvent event) {
        client.closeConnection();
        Platform.runLater(() -> {
            statusMessageLabel.setText("Disconnected from server.");
            setMoveButtonsEnabled(false);
            disconnectButton.setDisable(true);
            logger.info("Client disconnected.");
        });
    }

    private void setMoveButtonsEnabled(boolean enabled) {
        rockButton.setDisable(!enabled);
        paperButton.setDisable(!enabled);
        scissorsButton.setDisable(!enabled);
    }

    // Veřejná setter metoda pro přijetí instance Klienta
    public void setClient(karel.hudera.rps.client.Client client) {
        this.client = client;
        startMessageListener();
    }
}

