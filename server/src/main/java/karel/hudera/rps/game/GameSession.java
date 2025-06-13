package karel.hudera.rps.game;

import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.server.ClientHandler;
import karel.hudera.rps.utils.ServerLogger;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a game session between two players in the Rock-Paper-Scissors game.
 * Manages the game state, player moves, and determines the winner.
 *
 * @author Karel Hudera
 */
public class GameSession {
    private static final Logger logger = ServerLogger.INSTANCE;

    private final ClientHandler player1;
    private final ClientHandler player2;
    private volatile boolean isActive;

    /**
     * Creates a new game session between two players.
     *
     * @param player1 The first player
     * @param player2 The second player
     */
    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.isActive = true;

        logger.info("GameSession: Constructor entered.");
        try {
            logger.info("GameSession: Player 1 handle initialized. Info: " + player1.getClientInfo());
            logger.info("GameSession: Player 2 handle initialized. Info: " + player2.getClientInfo());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "GameSession: Error getting client info in constructor: " + e.getMessage(), e);
            this.isActive = false;
        }

        logger.info(String.format(Constants.LOG_GAME_STARTED,
                player1.getClientInfo(), player2.getClientInfo()));
    }

    /**
     * Runs the game session between the two players.
     * This method handles the game flow, including:
     * - Notifying players they've been matched
     * - Requesting and receiving player moves
     * - Determining and announcing the winner
     * - Handling disconnections and game completion
     */
    public void play() {
        logger.info("GameSession: Starting play method.");
        try {
            // Inform players they've been matched

            GameStart gameStartToPlayer1 = new GameStart(player2.getUsername());
            player1.sendMessage(gameStartToPlayer1);
            logger.info("📤 Sent GAME_START to " + player1.getUsername() + " (opponent: " + player2.getUsername() + ")");

            GameStart gameStartToPlayer2 = new GameStart(player1.getUsername());
            player2.sendMessage(gameStartToPlayer2);
            logger.info("📤 Sent GAME_START to " + player2.getUsername() + " (opponent: " + player1.getUsername() + ")");

            // Get player moves
            GameMessage move1 = player1.observeMessage();
            GameMessage move2 = player2.observeMessage();
            logger.info("AAAAAAAAAAAAAAAAAAAAAAAAA");
            logger.info("Player 1 made move: " + move1);
            logger.info("Player 2 made move: " + move2);

            // Check for disconnections
            if (move1 == null || move2 == null) {
                handleDisconnection();
                return;
            }

            // Determine winner and notify players
            //announceResult(move1, move2);

        } catch (IOException e) {
            logger.warning(String.format(Constants.ERROR_GAME_COMMUNICATION,
                    player1.getClientInfo(), player2.getClientInfo(), e.getMessage()));
            handleDisconnection();
        } finally {
            isActive = false;
        }
    }

    /**
     * Handles player disconnection during the game (legacy method).
     */
    private void handleDisconnection() {
        // Check which player(s) are disconnected and handle accordingly
        if (!player1.isConnected() && player2.isConnected()) {
            handlePlayerDisconnection(player1, player2);
        } else if (!player2.isConnected() && player1.isConnected()) {
            handlePlayerDisconnection(player2, player1);
        } else {
            // Both disconnected or other error
            logger.info("Both players disconnected or connection error occurred");
            isActive = false;
        }
    }

    private void handlePlayerDisconnection(ClientHandler disconnectedPlayer, ClientHandler remainingPlayer) {
        logger.info("Handling disconnection of " + disconnectedPlayer.getClientInfo());

        if (remainingPlayer.isConnected()) {
            OpponentDisconnected disconnectedMsg = new OpponentDisconnected(
                    disconnectedPlayer.getUsername()
            );
            remainingPlayer.sendMessage(disconnectedMsg);
            logger.info("📤 Sent OpponentDisconnected to " + remainingPlayer.getUsername());
        }

        isActive = false;
    }


    /**
     * Determines the winner of the game and notifies both players.
     *
     * @param move1 First player's move
     * @param move2 Second player's move
     */
    private void announceResult(String move1, String move2) {
        // Tell each player what the opponent chose
//        player1.sendMessage(String.format(Constants.MSG_OPPONENT_MOVE, move2));
//        player2.sendMessage(String.format(Constants.MSG_OPPONENT_MOVE, move1));

        // Determine winner
        if (move1.equalsIgnoreCase(move2)) {
            // Tie
//            player1.sendMessage(Constants.MSG_GAME_TIE);
//            player2.sendMessage(Constants.MSG_GAME_TIE);
            logger.info(String.format(Constants.LOG_GAME_TIE,
                    player1.getClientInfo(), player2.getClientInfo(), move1));
        } else if (isWinner(move1, move2)) {
            // Player 1 wins
//            player1.sendMessage(Constants.MSG_GAME_WIN);
//            player2.sendMessage(Constants.MSG_GAME_LOSS);
            logger.info(String.format(Constants.LOG_GAME_WINNER,
                    player1.getClientInfo(), player2.getClientInfo(), move1, move2));
        } else {
            // Player 2 wins
//            player1.sendMessage(New GameResult(Constants.MSG_GAME_LOSS));
//            player2.sendMessage(Constants.MSG_GAME_WIN);
            logger.info(String.format(Constants.LOG_GAME_WINNER,
                    player2.getClientInfo(), player1.getClientInfo(), move2, move1));
        }
    }

    /**
     * Determines if move1 beats move2 according to Rock-Paper-Scissors rules.
     *
     * @param move1 First player's move
     * @param move2 Second player's move
     * @return true if move1 beats move2, false otherwise
     */
    private boolean isWinner(String move1, String move2) {
        return (move1.equalsIgnoreCase(Constants.MOVE_ROCK) && move2.equalsIgnoreCase(Constants.MOVE_SCISSORS)) ||
                (move1.equalsIgnoreCase(Constants.MOVE_SCISSORS) && move2.equalsIgnoreCase(Constants.MOVE_PAPER)) ||
                (move1.equalsIgnoreCase(Constants.MOVE_PAPER) && move2.equalsIgnoreCase(Constants.MOVE_ROCK));
    }

    /**
     * Checks if the game session is still active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return isActive && player1.isConnected() && player2.isConnected();
    }
}