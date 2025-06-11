package karel.hudera.rps.game;

import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.server.ClientHandler;
import karel.hudera.rps.utils.ServerLogger;

import java.io.IOException;
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
        try {
            // Inform players they've been matched
            player1.sendMessage(Constants.MSG_OPPONENT_FOUND);
            player2.sendMessage(Constants.MSG_OPPONENT_FOUND);

            // Request moves from both players
            player1.sendMessage(Constants.MSG_REQUEST_MOVE);
            player2.sendMessage(Constants.MSG_REQUEST_MOVE);

            // Get player moves
            String move1 = player1.observeMessage();
            String move2 = player2.observeMessage();

            // Check for disconnections
            if (move1 == null || move2 == null) {
                handleDisconnection();
                return;
            }

            // Validate moves
            if (!isValidMove(move1) || !isValidMove(move2)) {
                handleInvalidMove(move1, move2);
                return;
            }

            // Determine winner and notify players
            announceResult(move1, move2);

        } catch (IOException e) {
            logger.warning(String.format(Constants.ERROR_GAME_COMMUNICATION,
                    player1.getClientInfo(), player2.getClientInfo(), e.getMessage()));
            handleDisconnection();
        } finally {
            isActive = false;
        }
    }

    /**
     * Determines if a move is valid (rock, paper, or scissors).
     *
     * @param move The player's move
     * @return true if valid, false otherwise
     */
    private boolean isValidMove(String move) {
        return move.equalsIgnoreCase(Constants.MOVE_ROCK) ||
                move.equalsIgnoreCase(Constants.MOVE_PAPER) ||
                move.equalsIgnoreCase(Constants.MOVE_SCISSORS);
    }

    /**
     * Handles the case when one or both players make invalid moves.
     *
     * @param move1 First player's move
     * @param move2 Second player's move
     */
    private void handleInvalidMove(String move1, String move2) {
        if (!isValidMove(move1)) {
            player1.sendMessage(Constants.MSG_INVALID_MOVE);
            player2.sendMessage(Constants.MSG_OPPONENT_INVALID);
        }
        if (!isValidMove(move2)) {
            player2.sendMessage(Constants.MSG_INVALID_MOVE);
            player1.sendMessage(Constants.MSG_OPPONENT_INVALID);
        }
    }

    /**
     * Handles player disconnection during the game.
     */
    private void handleDisconnection() {
        if (player1.isConnected()) {
            player1.sendMessage(Constants.MSG_OPPONENT_DISCONNECTED);
        }
        if (player2.isConnected()) {
            player2.sendMessage(Constants.MSG_OPPONENT_DISCONNECTED);
        }
    }

    /**
     * Determines the winner of the game and notifies both players.
     *
     * @param move1 First player's move
     * @param move2 Second player's move
     */
    private void announceResult(String move1, String move2) {
        // Tell each player what the opponent chose
        player1.sendMessage(String.format(Constants.MSG_OPPONENT_MOVE, move2));
        player2.sendMessage(String.format(Constants.MSG_OPPONENT_MOVE, move1));

        // Determine winner
        if (move1.equalsIgnoreCase(move2)) {
            // Tie
            player1.sendMessage(Constants.MSG_GAME_TIE);
            player2.sendMessage(Constants.MSG_GAME_TIE);
            logger.info(String.format(Constants.LOG_GAME_TIE,
                    player1.getClientInfo(), player2.getClientInfo(), move1));
        } else if (isWinner(move1, move2)) {
            // Player 1 wins
            player1.sendMessage(Constants.MSG_GAME_WIN);
            player2.sendMessage(Constants.MSG_GAME_LOSS);
            logger.info(String.format(Constants.LOG_GAME_WINNER,
                    player1.getClientInfo(), player2.getClientInfo(), move1, move2));
        } else {
            // Player 2 wins
            player1.sendMessage(Constants.MSG_GAME_LOSS);
            player2.sendMessage(Constants.MSG_GAME_WIN);
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