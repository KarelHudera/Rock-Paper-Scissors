package karel.hudera.rps;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import karel.hudera.rps.client.Client;
import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.game.GameAction;
import karel.hudera.rps.game.GameMessage;
import karel.hudera.rps.game.GameState;
import karel.hudera.rps.game.Move;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static karel.hudera.rps.game.GameState.GameStatus.*;

public class GameController {

    private static final Logger logger = Logger.getLogger("GameLogger");

    private ExecutorService executorService; //pro asynchronní čtení zpráv
    private final Client gameClient;
    @FXML
    private StackPane loadingPane;

    @FXML
    private HBox buttonsPane; //tady budou buttons RPS

    @FXML
    private StackPane movePlayedPane; //text "tah odeslán"

    @FXML
    private Button rockButton, paperButton, scissorsButton;

    @FXML
    private Text selectedMoveText;

    @FXML
    private Label statusMessageLabel; //stavové zpráv od serveru
    @FXML
    private Label scoreLabel;         //skóre
    @FXML
    private Label opponentLabel;      //jméno protihráče
    @FXML
    private TextArea gameLogArea;     //logování událostí hry

    public GameController(Client client) {
        this.gameClient = client;
        logger.info("GameController initialized for user: " + gameClient.getLoggedInUsername());
    }

    @FXML
    private void initialize() {
        loadingPane.setVisible(false);
        buttonsPane.setVisible(true);
        loadingPane.setVisible(true); // Na začátku ukazujeme loading screen
        buttonsPane.setVisible(false); // Tlačítka jsou skrytá
        movePlayedPane.setVisible(false); // Move played screen je skrytý
        setMoveButtonsEnabled(false); // Tlačítka jsou zakázaná

        // Počáteční stavové zprávy
        statusMessageLabel.setText(Constants.WAITING_FOR_OPPONENT);
        scoreLabel.setText("Score: 0 - 0");
        opponentLabel.setText("Waiting for opponent...");
        if (gameLogArea != null) { // Zajistí, že logArea existuje (FXML)
            gameLogArea.setEditable(false);
            gameLogArea.setWrapText(true);
            appendToGameLog("Game started for user: " + gameClient.getLoggedInUsername());
        }


        // Spustit vlákno pro příjem zpráv od serveru
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::listenForServerMessages);
    }
    private void listenForServerMessages() {
        try {
            while (true) {
                // readServerMessage je blokující, čeká na zprávu
                GameMessage message = gameClient.readServerMessage();
                if (message instanceof GameState) {
                    GameState gameState = (GameState) message;
                    logger.info("Received GameState: " + gameState.getStatus() + " - " + gameState.getMessage());

                    // Všechny aktualizace UI musí probíhat na JavaFX Application Thread!
                    Platform.runLater(() -> handleGameState(gameState));
                } else {
                    logger.warning("Received unknown message type: " + message.getClass().getName());
                    appendToGameLog("Received unknown message from server: " + message.getClass().getSimpleName());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error listening for server messages: " + e.getMessage(), e);
            appendToGameLog("Connection lost or error: " + e.getMessage());
            // Zde byste mohli přesměrovat uživatele zpět na přihlašovací obrazovku
            Platform.runLater(this::handleConnectionLoss);
        } finally {
            // Zavřeme spojení, pokud vlákno skončí (např. po chybě)
            if (gameClient != null) {
                gameClient.closeConnection();
                logger.info("Game client connection closed in listener thread.");
            }
        }
    }

    private void handleGameState(GameState gameState) {
        statusMessageLabel.setText(gameState.getMessage()); // Zobrazí hlavní zprávu ze serveru
        appendToGameLog("Server: " + gameState.getMessage());

        // Aktualizovat jména hráčů a skóre
        if (gameState.getPlayer1Id() != null && gameState.getPlayer2Id() != null) {
            String myId = gameClient.getLoggedInUsername();
            int myScore = 0;
            int opponentScore = 0;
            String opponentName = "";

            if (myId != null && myId.equals(gameState.getPlayer1Id())) {
                myScore = gameState.getPlayer1Score();
                opponentScore = gameState.getPlayer2Score();
                opponentName = gameState.getPlayer2Id();
            } else if (myId != null && myId.equals(gameState.getPlayer2Id())) {
                myScore = gameState.getPlayer2Score();
                opponentScore = gameState.getPlayer1Score();
                opponentName = gameState.getPlayer1Id();
            }

            opponentLabel.setText("Playing against: " + opponentName);
            scoreLabel.setText("Score: " + myScore + " - " + opponentScore);
        }


        switch (gameState.getStatus()) {
            case WAITING_FOR_PLAYERS:
                loadingPane.setVisible(true); // Ukazujeme loading
                buttonsPane.setVisible(false); // Tlačítka jsou skrytá
                setMoveButtonsEnabled(false);
                appendToGameLog("Waiting for opponent to connect...");
                break;
            case LOBBY_READY:
                loadingPane.setVisible(false); // Skryjeme loading
                buttonsPane.setVisible(true); // Ukážeme tlačítka (ale ještě zakázané)
                setMoveButtonsEnabled(false);
                statusMessageLabel.setText(Constants.WAITING_FOR_GAME_START); // Např. "Protihráč připojen. Čekáme na spuštění hry."
                appendToGameLog("Opponent connected! Game will start soon.");
                break;
            case ROUND_STARTED:
                loadingPane.setVisible(false); // Skryjeme loading
                buttonsPane.setVisible(true); // Ukážeme tlačítka
                setMoveButtonsEnabled(true); // Povolíme hráči provést tah
                movePlayedPane.setVisible(false); // Skryjeme zprávu o odehraném tahu
                selectedMoveText.setText(""); // Vyčistíme text předchozího tahu
                appendToGameLog("New round! Make your move.");
                statusMessageLabel.setText(Constants.MAKE_YOUR_MOVE);
                break;
            case PLAYER_MADE_CHOICE:
                setMoveButtonsEnabled(false); // Hráč již udělal tah, zakázat tlačítka
                buttonsPane.setVisible(false);
                movePlayedPane.setVisible(true); // Zobrazíme, že tah byl odeslán
                appendToGameLog("You made your choice. Waiting for opponent...");
                statusMessageLabel.setText(Constants.WAITING_FOR_OPPONENT_MOVE);
                break;
            case ROUND_ENDED:
                setMoveButtonsEnabled(false); // Kolo skončilo, tahy zakázané
                buttonsPane.setVisible(false); // Tlačítka jsou skrytá
                movePlayedPane.setVisible(true); // Zobrazíme výsledky (nebo přejdeme na jiný panel)
                appendToGameLog("Round ended! Result: " + gameState.getRoundResult());
                appendToGameLog("Your choice: " + gameState.getYourChoice() + ", Opponent's choice: " + gameState.getOpponentChoice());
                statusMessageLabel.setText(gameState.getMessage()); // Může obsahovat "Vyhrál jsi!", "Prohrál jsi!"
                // Zde můžete aktualizovat obrázky tahů, pokud je máte
                break;
            case GAME_OVER:
                setMoveButtonsEnabled(false); // Hra skončila
                buttonsPane.setVisible(false);
                movePlayedPane.setVisible(true); // Zobrazíme závěrečný stav
                appendToGameLog("GAME OVER! " + gameState.getMessage()); // Zpráva už obsahuje vítěze
                statusMessageLabel.setText(gameState.getMessage());
                // Možná zobrazit alert s výsledkem hry a nabídnout novou hru nebo návrat do menu
                // handleConnectionLoss(); // Zde byste mohli vrátit uživatele na login nebo zobrazit závěr
                break;
        }
    }


    private void setMoveButtonsEnabled(boolean enable) {
        rockButton.setDisable(!enable);
        paperButton.setDisable(!enable);
        scissorsButton.setDisable(!enable);
    }

    private void appendToGameLog(String message) {
        // Použijte Platform.runLater, protože UI aktualizace musí probíhat na JavaFX Application Thread
        if (gameLogArea != null) {
            Platform.runLater(() -> gameLogArea.appendText(message + "\n"));
        }
    }

    // Odstraněny metody onOpponentConnected() a onMovePlayed() - logika je nyní v handleGameState
    // Tyto metody byly zbytečné, protože stav hry řídí server přes GameState

    @FXML
    private void onRockClick() {
        sendPlayerChoice(Move.ROCK);
    }

    @FXML
    private void onPaperClick() {
        sendPlayerChoice(Move.PAPER);
    }

    @FXML
    private void onScissorsClick() {
        sendPlayerChoice(Move.SCISSORS);
    }

    private void sendPlayerChoice(Move choice) {
        logger.info("Player chose: " + choice.name());
        selectedMoveText.setText("You chose: " + choice.name()); // Aktualizuje text zobrazeného tahu

        // Vytvoření GameAction objektu pro odeslání na server
        GameAction action = new GameAction(gameClient.getLoggedInUsername(), choice);
        try {
            gameClient.sendToServer(action);
            setMoveButtonsEnabled(false); // Zakázat tlačítka po odeslání tahu
            buttonsPane.setVisible(false); // Skrýt tlačítka
            movePlayedPane.setVisible(true); // Zobrazit "tah byl odehrán"
            appendToGameLog("Sent your choice: " + choice.name());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to send player choice: " + e.getMessage(), e);
            appendToGameLog("Error sending choice: " + e.getMessage());
            statusMessageLabel.setText("Error sending move. Try again?");
            // Možná znovu povolit tlačítka nebo zkusit znovu připojení
        }
    }

    /**
     * Metoda pro ošetření ztráty spojení (např. přesun na login screen).
     */
    private void handleConnectionLoss() {
        // Implementujte logiku pro návrat na přihlašovací obrazovku
        // nebo zobrazení chybové zprávy uživateli
        logger.warning("Connection lost. Returning to login screen.");
        // Příklad: Návrat na login screen
        try {
            // Zde byste normálně načetli login-view.fxml a změnili scénu
            // Kvůli jednoduchosti tohoto příkladu a absenci Main třídy zde jen log
            // V reálné aplikaci byste to dělali například přes MainApp referenci nebo event bus
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to return to login screen: " + e.getMessage(), e);
        }
    }

    /**
     * Metoda pro čištění zdrojů, když se GameController přestane používat.
     */
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdownNow(); // Pokusí se okamžitě zastavit vlákna
            logger.info("GameController executor service shut down.");
        }
        // Client je již uzavřen v listenForServerMessages.finally, ale pro jistotu
        if (gameClient != null) {
            gameClient.closeConnection(); // Zajistí, že spojení je uzavřeno
            logger.info("GameClient connection explicitly closed during shutdown.");
        }
    }


    /**
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
    **/
}