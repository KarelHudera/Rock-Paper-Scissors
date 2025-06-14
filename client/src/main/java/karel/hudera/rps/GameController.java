package karel.hudera.rps;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import karel.hudera.rps.client.Client;
import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.game.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import karel.hudera.rps.StartClient;


public class GameController implements Initializable {

    private static final Logger logger = Logger.getLogger("ClientLogger");

    @FXML private Label statusMessageLabel;
    @FXML private Label yourUsernameLabel;
    @FXML private Label opponentUsernameLabel;
    @FXML private Label yourScoreLabel;
    @FXML private Label opponentScoreLabel;
    @FXML private Label roundResultLabel;
    @FXML private Label waitingForOpponentMoveLabel;

    //final results
    @FXML private VBox resultOverlayContainer;
    @FXML private Label finalYourMoveLabel;
    @FXML private Label finalOpponentMoveLabel;
    @FXML private Label finalRoundResultLabel;
    @FXML
    private VBox gameContentContainer;
    @FXML
    private VBox waitingOverlayContainer;
    //final result
    @FXML private VBox finalResultOverlayContainer;
    @FXML private Label finalGameOutcomeLabel;
    @FXML private Label finalGameScoreLabel;

    @FXML private Button rockButton;
    @FXML private Button paperButton;
    @FXML private Button scissorsButton;
    @FXML private Button disconnectButton;
    //logování událostí hry
    private Client client;
    private String loggedInUsername;
    private String opponentUsername;

    // Skóre
    private int yourScore = 0;
    private int opponentScore = 0;
    private final int CLIENT_MAX_ROUNDS = 3; // <-- Hardcoded na klientovi, podle serveru
    private int clientCurrentRound = 0;

    public GameController() {}

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets initial UI visibility and starts the thread for listening to server messages.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        gameContentContainer.setVisible(true);
        waitingOverlayContainer.setVisible(false);
        resultOverlayContainer.setVisible(false);
        finalResultOverlayContainer.setVisible(false);
        clientCurrentRound = 0;

        yourUsernameLabel.setText(loggedInUsername);
        statusMessageLabel.setText(Constants.WAITING_FOR_OPPONENT);
        opponentUsernameLabel.setText("...");
        yourScoreLabel.setText("0");
        opponentScoreLabel.setText("0");
        roundResultLabel.setText("");
        waitingForOpponentMoveLabel.setText("Pick your move!");

        setMoveButtonsEnabled(false);
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

            logger.info("GameStart received. Before enabling buttons - Rock button disabled: " + rockButton.isDisable());
            setMoveButtonsEnabled(true); // Povol tlačítka pro tah
            logger.info("GameStart received. After enabling buttons - Rock button disabled: " + rockButton.isDisable());
            logger.info("GameStart received. gameContentContainer visible: " + gameContentContainer.isVisible() + ", waitingOverlayContainer visible: " + waitingOverlayContainer.isVisible() + ", resultOverlayContainer visible: " + resultOverlayContainer.isVisible() + ", finalResultOverlayContainer visible: " + finalResultOverlayContainer.isVisible());

            statusMessageLabel.setText("Game Started!");
            opponentUsernameLabel.setText(opponentUsername);
            yourScore = 0;
            opponentScore = 0;
            yourScoreLabel.setText("0");
            opponentScoreLabel.setText("0");
            roundResultLabel.setText("");
            waitingForOpponentMoveLabel.setText("Pick your move!");


        } else if (message instanceof RoundResult) {
            RoundResult roundResult = (RoundResult) message;

            yourScore = roundResult.getPlayer1Score();
            opponentScore = roundResult.getPlayer2Score();
            yourScoreLabel.setText(String.valueOf(yourScore));
            opponentScoreLabel.setText(String.valueOf(opponentScore));

            // Zobrazení tahů
            finalYourMoveLabel.setText("You: " + roundResult.getYourMove().name());
            finalOpponentMoveLabel.setText("Opponent: " + roundResult.getOpponentMove().name());


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
            finalRoundResultLabel.setText(resultText);

            // Skryj herní a čekací overlay, zobraz výsledkový overlay
            gameContentContainer.setVisible(false);
            waitingOverlayContainer.setVisible(false);
            resultOverlayContainer.setVisible(true);

            setMoveButtonsEnabled(false);

            clientCurrentRound++;

            if (clientCurrentRound < CLIENT_MAX_ROUNDS) {
                PauseTransition delay = new PauseTransition(Duration.seconds(2));
                delay.setOnFinished(event -> {
                    resultOverlayContainer.setVisible(false); // Skryj výsledkový overlay
                    gameContentContainer.setVisible(true);   // Zobraz hlavní herní obrazovku
                    waitingForOpponentMoveLabel.setText("Pick your move for round " + (clientCurrentRound + 1) + "!");
                    roundResultLabel.setText("");
                    setMoveButtonsEnabled(true);
                });
                delay.play();
            } else {
                PauseTransition finalDelay = new PauseTransition(Duration.seconds(3));
                finalDelay.setOnFinished(event -> {

                    gameContentContainer.setVisible(false);
                    waitingOverlayContainer.setVisible(false);
                    resultOverlayContainer.setVisible(false);

                    finalResultOverlayContainer.setVisible(true);

                    String finalOutcomeText;
                    String finalOutcomeStyle = "";
                    if (yourScore > opponentScore) {
                        finalOutcomeText = "YOU WIN THE GAME!";
                        finalOutcomeStyle = "-fx-text-fill: #28a745;";
                    } else if (opponentScore > yourScore) {
                        finalOutcomeText = "YOU LOSE THE GAME!";
                        finalOutcomeStyle = "-fx-text-fill: #dc3545;";
                    } else {
                        finalOutcomeText = "IT'S A TIE GAME!";
                        finalOutcomeStyle = "-fx-text-fill: #ffc107;";
                    }
                    finalGameOutcomeLabel.setText(finalOutcomeText);
                    finalGameOutcomeLabel.setStyle(finalOutcomeStyle);
                    finalGameScoreLabel.setText(String.format("Final Score: You %d - Opponent %d", yourScore, opponentScore));
                });
                finalDelay.play();
        } }
    }

    @FXML
    private void handleMove(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String moveName = "";
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
            GameMove gameMove = new GameMove(move);
            client.sendToServer(gameMove);
            statusMessageLabel.setText("You chose " + moveName + ".");
            waitingForOpponentMoveLabel.setText("Waiting for opponent's move...");
            setMoveButtonsEnabled(false);
            roundResultLabel.setText("");

            // změna UI
            Platform.runLater(() -> {
                gameContentContainer.setVisible(false);
                waitingOverlayContainer.setVisible(true);
                resultOverlayContainer.setVisible(false);
                waitingForOpponentMoveLabel.setText("Waiting for opponent's move...");
            });

            setMoveButtonsEnabled(false);
            roundResultLabel.setText("");
        } catch (IOException e) {
            logger.severe("Failed to send move: " + e.getMessage());
            statusMessageLabel.setText("Error sending move. Connection lost?");
            setMoveButtonsEnabled(false);
        }
    }

    /**
     * Ovládá disconnect button.
     * Volá metodu klienta pro odpojení od serveru a posílá mu zrávu o ukončení spojení.
     * **/
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


    public void handlePlayAgain(ActionEvent actionEvent) {
        logger.info("Play again");
    }
}

