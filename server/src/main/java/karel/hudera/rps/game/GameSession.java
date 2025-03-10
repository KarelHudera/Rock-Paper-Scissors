package karel.hudera.rps.game;

import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.server.ClientHandler;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Represents a game session between two players.
 * Handles the flow of the game, determining the winner and sending game results.
 */
public class GameSession {

    /**
     * Logger instance for logging server activity
     */
    private static Logger logger;
    private final ClientHandler player1;
    private final ClientHandler player2;

    /**
     * Constructs a new {@code GameSession} between two players.
     *
     * @param player1 The first player in the game session.
     * @param player2 The second player in the game session.
     * @param logger  The logger instance for logging game activity.
     */
    public GameSession(ClientHandler player1, ClientHandler player2, Logger logger) {
        this.player1 = player1;
        this.player2 = player2;
        GameSession.logger = logger;
    }

    /**
     * Starts the game session, notifying both players and handling the game logic.
     */
    public void start() {
        logger.info(String.format(Constants.LOG_GAME_SESSION_STARTED, player1.username, player2.username));
        new Thread(this::playGame).start();
    }

    private void playGame() {
        try {
            player1.sendMessage(Constants.GAME_STARTED + player2.username);
            player2.sendMessage(Constants.GAME_STARTED + player1.username);

            Move move1 = (Move) player1.input.readObject();
            Move move2 = (Move) player2.input.readObject();

            String result = determineWinner(move1, move2);
            GameResult gameResult = new GameResult(player1.username, move1.name(), player2.username, move2.name(), result);

            player1.sendMessage(gameResult);
            player2.sendMessage(gameResult);

            logger.info(Constants.LOG_GAME_RESULT + gameResult);

        } catch (IOException | ClassNotFoundException e) {
            logger.severe(Constants.LOG_GAME_ERROR + e.getMessage());
        } finally {
            logger.info("HHHHHHHHHHHHHA");
            //logger.info(Constants.LOG_CLIENT_CLOSED);
           // player1.closeResources();
          //  player2.closeResources();
        }
    }

    /**
     * Determines the winner of the game based on the players' moves.
     *
     * @param move1 The move of player1.
     * @param move2 The move of player2.
     * @return The result of the game (either a winner or a draw).
     */
    private String determineWinner(Move move1, Move move2) {
        if (move1 == move2) {
            return Constants.DRAW;
        }
        if ((move1 == Move.ROCK && move2 == Move.SCISSORS) ||
                (move1 == Move.SCISSORS && move2 == Move.PAPER) ||
                (move1 == Move.PAPER && move2 == Move.ROCK)) {
            return player1.username + Constants.WINS;
        }
        return player2.username + Constants.WINS;
    }
}