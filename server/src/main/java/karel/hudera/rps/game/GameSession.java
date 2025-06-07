package karel.hudera.rps.game;

import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.server.ClientHandler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a game session between two players.
 * Handles the flow of the game, determining the winner and sending game results.
 */
public class GameSession implements Runnable {


    /**
     * Logger instance for logging server activity
     */
    private static final Logger logger = Logger.getLogger("GameSessionLogger"); // Vlastní logger pro GameSession
    private final String sessionId; // Unikátní ID pro každou hru
    private final ClientHandler player1;
    private final ClientHandler player2;

    private Move player1Choice;
    private Move player2Choice;
    private int player1Score;
    private int player2Score;
    private GameState.GameStatus currentStatus;

    // Fronty pro přijímání tahů od hráčů (volané z ClientHandleru)
    private final BlockingQueue<GameAction> player1Actions = new LinkedBlockingQueue<>();
    private final BlockingQueue<GameAction> player2Actions = new LinkedBlockingQueue<>();


    /**
     * Constructs a new {@code GameSession} between two players.
     *
     * @param player1 The first player in the game session.
     * @param player2 The second player in the game session.
     */
    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.sessionId = UUID.randomUUID().toString(); // Generujeme unikátní ID pro hru
        this.player1 = player1;
        this.player2 = player2;
        this.player1Score = 0;
        this.player2Score = 0;
        this.currentStatus = GameState.GameStatus.LOBBY_READY; // Počáteční stav

        // Nastavit referenci GameSession v ClientHandleru, aby Handler věděl, kam posílat akce
        this.player1.setGameSession(this);
        this.player2.setGameSession(this);

        logger.info(String.format(Constants.LOG_GAME_SESSION_CREATED, player1.getUsername(), player2.getUsername(), sessionId));
    }


    /**
     * Called by ClientHandler to submit a player's action to this game session.
     * @param action The GameAction received from a client.
     */
    public void submitPlayerAction(GameAction action) {
        if (action.getPlayerId().equals(player1.getUsername())) {
            try {
                player1Actions.put(action); // Přidat tah do fronty hráče 1
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(Level.SEVERE, "Interrupted while submitting player1 action: " + e.getMessage(), e);
            }
        } else if (action.getPlayerId().equals(player2.getUsername())) {
            try {
                player2Actions.put(action); // Přidat tah do fronty hráče 2
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(Level.SEVERE, "Interrupted while submitting player2 action: " + e.getMessage(), e);
            }
        } else {
            logger.warning("Received action from unknown player: " + action.getPlayerId() + " for session " + sessionId);
        }
    }

    @Override
    public void run() {
        logger.info(String.format(Constants.LOG_GAME_SESSION_STARTED, player1.getUsername(), player2.getUsername()));
        try {
            // Informovat hráče, že hra začala a čekáme na tahy
            sendGameStateToAll(GameState.GameStatus.ROUND_STARTED, Constants.MAKE_YOUR_MOVE);

            // Hlavní herní smyčka
            while (true) {
                // Čekání na tahy obou hráčů
                GameAction p1Action = player1Actions.take(); // Blokuje, dokud není k dispozici tah
                GameAction p2Action = player2Actions.take(); // Blokuje, dokud není k dispozici tah

                player1Choice = p1Action.getChoice();
                player2Choice = p2Action.getChoice();

                logger.info(String.format("Player %s chose %s, Player %s chose %s",
                        player1.getUsername(), player1Choice, player2.getUsername(), player2Choice));

                // Vyhodnocení kola
                String roundResultMsg = determineWinner(player1Choice, player2Choice);

                // Kontrola, zda hra neskončila (např. jeden z hráčů dosáhl N bodů)
                if (checkGameOver()) {
                    sendGameOverState();
                    break; // Ukončí smyčku hry
                }

                // Odeslání výsledků kola
                sendRoundResultState(roundResultMsg);

                // Krátká pauza před dalším kolem, nebo čekání na "ready" zprávu od klientů
                Thread.sleep(2000); // Počkejte 2 sekundy před začátkem nového kola

                // Zahájení nového kola
                sendGameStateToAll(GameState.GameStatus.ROUND_STARTED, Constants.MAKE_YOUR_MOVE);
            }
        } catch (InterruptedException e) {
            logger.log(Level.INFO, "Game session " + sessionId + " interrupted: " + e.getMessage(), e);
            sendGameStateToAll(GameState.GameStatus.GAME_OVER, Constants.GAME_INTERRUPTED);
        } finally {
            logger.info("Game session " + sessionId + " ended.");
            // Informovat hráče o konci hry a případně je odpojit nebo vrátit do lobby
            cleanupGame();
        }
    }

    private String determineWinner(Move move1, Move move2) {
        String resultMsg;
        String winner = "DRAW";

        if (move1 == move2) {
            resultMsg = Constants.DRAW;
        } else if ((move1 == Move.ROCK && move2 == Move.SCISSORS) ||
                (move1 == Move.SCISSORS && move2 == Move.PAPER) ||
                (move1 == Move.PAPER && move2 == Move.ROCK)) {
            player1Score++;
            resultMsg = player1.getUsername() + Constants.WINS_ROUND;
            winner = player1.getUsername();
        } else {
            player2Score++;
            resultMsg = player2.getUsername() + Constants.WINS_ROUND;
            winner = player2.getUsername();
        }
        logger.info(String.format("Round result for session %s: %s vs %s -> %s. Scores: %d-%d",
                sessionId, player1.getUsername(), player2.getUsername(), winner, player1Score, player2Score));
        return resultMsg;
    }

    private boolean checkGameOver() {
        // Implementujte logiku konce hry, např. kdo dosáhl 3 bodů
        int maxScore = 3; // Příklad
        if (player1Score >= maxScore || player2Score >= maxScore) {
            return true;
        }
        return false;
    }

    private void sendGameOverState() {
        String finalMessage;
        if (player1Score > player2Score) {
            finalMessage = player1.getUsername() + Constants.WINS_GAME;
        } else if (player2Score > player1Score) {
            finalMessage = player2.getUsername() + Constants.WINS_GAME;
        } else {
            finalMessage = Constants.GAME_DRAW;
        }

        GameState finalState = new GameState(GameState.GameStatus.GAME_OVER, finalMessage);
        finalState.setPlayerIds(player1.getUsername(), player2.getUsername());
        finalState.setScores(player1Score, player2Score);
        finalState.setYourChoice(null); // No choice for game over
        finalState.setOpponentChoice(null); // No choice for game over
        finalState.setRoundResult(finalMessage); // Use game over message as round result

        try {
            player1.sendMessage(finalState);
            player2.sendMessage(finalState);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to send GAME_OVER state for session " + sessionId + ": " + e.getMessage(), e);
        }
    }

    private void sendRoundResultState(String message) {
        // Stav pro hráče 1
        GameState p1State = new GameState(GameState.GameStatus.ROUND_ENDED, message);
        p1State.setPlayerIds(player1.getUsername(), player2.getUsername());
        p1State.setScores(player1Score, player2Score);
        p1State.setYourChoice(player1Choice); // Vlastní tah
        p1State.setOpponentChoice(player2Choice); // Tah protihráče
        p1State.setRoundResult(message); // Výsledek kola pro zobrazení (např. "Vyhrál jsi!")

        // Stav pro hráče 2 (inverted choices)
        GameState p2State = new GameState(GameState.GameStatus.ROUND_ENDED, message);
        p2State.setPlayerIds(player1.getUsername(), player2.getUsername());
        p2State.setScores(player1Score, player2Score);
        p2State.setYourChoice(player2Choice); // Vlastní tah
        p2State.setOpponentChoice(player1Choice); // Tah protihráče
        p2State.setRoundResult(message); // Výsledek kola pro zobrazení

        try {
            player1.sendMessage(p1State);
            player2.sendMessage(p2State);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to send ROUND_ENDED state for session " + sessionId + ": " + e.getMessage(), e);
        }
    }

    private void sendGameStateToAll(GameState.GameStatus status, String message) {
        GameState state = new GameState(status, message);
        state.setPlayerIds(player1.getUsername(), player2.getUsername());
        state.setScores(player1Score, player2Score);
        // Další relevantní data podle stavu (např. choices, roundResult)
        try {
            player1.sendMessage(state);
            player2.sendMessage(state);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to send GameState to all for session " + sessionId + ": " + e.getMessage(), e);
        }
    }

    private void cleanupGame() {
        // Po skončení hry (nebo přerušení) můžete klienty buď odpojit,
        // nebo je vrátit do lobby čekající na novou hru.
        // Zde můžete volat gameManager.unregisterPlayer(player1), gameManager.unregisterPlayer(player2)
        // a gameManager by je pak mohl vrátit do seznamu čekajících.
        // Pro jednoduchost je můžeme nechat připojené a čekat na další instrukce nebo je server odpojí.
    }

    // Gettery pro přístup k ClientHandlerům, pokud je GameManager potřebuje
    public ClientHandler getPlayer1() { return player1; }
    public ClientHandler getPlayer2() { return player2; }
    public String getSessionId() { return sessionId; }
}