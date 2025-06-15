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
        int player1Score = 0;
        int player2Score = 0;

        try {
            player1.sendMessage(new GameStart(player2.getUsername()));
            player2.sendMessage(new GameStart(player1.getUsername()));

            for (int round = 1; round <= 3; round++) {
                logger.info("GameSession: Starting round " + round);

                GameMessage message1 = player1.observeMessage();
                GameMessage message2 = player2.observeMessage();

                if (!(message1 instanceof GameMove) || !(message2 instanceof GameMove)) {
                    logger.warning("GameSession: Invalid or null GameMove received.");
                    handleDisconnection();
                    return;
                }

                Move move1 = ((GameMove) message1).getMove();
                Move move2 = ((GameMove) message2).getMove();
                Result result1, result2;

                if (move1 == move2) {
                    result1 = result2 = Result.DRAW;
                } else if (isWinner(move1, move2)) {
                    result1 = Result.WIN;
                    result2 = Result.LOSE;
                    player1Score++;
                } else {
                    result1 = Result.LOSE;
                    result2 = Result.WIN;
                    player2Score++;
                }

                logger.info(String.format("Round %d Result: %s (%s) vs %s (%s) -> [%s : %s]",
                        round,
                        player1.getUsername(), move1,
                        player2.getUsername(), move2,
                        result1, result2));

                // Notify players about round results
                player1.sendMessage(new RoundResult(move1, move2, result1, player1Score, player2Score));
                player2.sendMessage(new RoundResult(move2, move1, result2, player2Score, player1Score));
            }

            // Determine and send final result
            sendFinalResults(player1Score, player2Score);

        } catch (IOException e) {
            logger.warning(String.format(Constants.ERROR_GAME_COMMUNICATION,
                    player1.getClientInfo(), player2.getClientInfo(), e.getMessage()));
            handleDisconnection();
        } finally {
            isActive = false;
        }
    }

    private void sendFinalResults(int player1Score, int player2Score) {
        String resultP1, resultP2;

        if (player1Score > player2Score) {
            resultP1 = Constants.MSG_GAME_WIN;
            resultP2 = Constants.MSG_GAME_LOSS;
        } else if (player1Score < player2Score) {
            resultP1 = Constants.MSG_GAME_LOSS;
            resultP2 = Constants.MSG_GAME_WIN;
        } else {
            resultP1 = resultP2 = Constants.MSG_GAME_TIE;
        }

        player1.sendMessage(new GameResult(player1.getUsername(), String.valueOf(player1Score),
                player2.getUsername(), String.valueOf(player2Score), resultP1));

        player2.sendMessage(new GameResult(player2.getUsername(), String.valueOf(player2Score),
                player1.getUsername(), String.valueOf(player1Score), resultP2));

        logger.info(String.format("GameSession: Final result sent. [%s: %d] vs [%s: %d]",
                player1.getUsername(), player1Score,
                player2.getUsername(), player2Score));
    }

    private void handleDisconnection() {
        if (!player1.isConnected() && player2.isConnected()) {
            handlePlayerDisconnection(player1, player2);
        } else if (!player2.isConnected() && player1.isConnected()) {
            handlePlayerDisconnection(player2, player1);
        } else {
            logger.info("Both players disconnected or error occurred.");
        }

        isActive = false;
    }

    private void handlePlayerDisconnection(ClientHandler disconnected, ClientHandler remaining) {
        logger.info("Player disconnected: " + disconnected.getClientInfo());

        if (remaining.isConnected()) {
            remaining.sendMessage(new OpponentDisconnected(disconnected.getUsername()));
            logger.info("📤 Sent OpponentDisconnected to " + remaining.getUsername());
        }
    }

    /**
     * Determines if move1 beats move2 according to Rock-Paper-Scissors rules.
     *
     * @param move1 First player's move
     * @param move2 Second player's move
     * @return true if move1 beats move2, false otherwise
     */
    private boolean isWinner(Move move1, Move move2) {
        return (move1 == Move.ROCK && move2 == Move.SCISSORS) ||
                (move1 == Move.SCISSORS && move2 == Move.PAPER) ||
                (move1 == Move.PAPER && move2 == Move.ROCK);
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
