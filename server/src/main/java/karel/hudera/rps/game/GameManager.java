package karel.hudera.rps.game;

import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.server.ClientHandler;
import karel.hudera.rps.utils.ServerLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Manages the matching of players and creation of game sessions for the Rock-Paper-Scissors game.
 * This class is responsible for:
 * <ul>
 *   <li>Maintaining a queue of waiting players</li>
 *   <li>Matching players to create game sessions</li>
 *   <li>Managing active game sessions</li>
 *   <li>Handling player reconnection to the waiting queue after a game</li>
 * </ul>
 *
 * @author Karel Hudera
 */
public class GameManager {
    private static final Logger logger = ServerLogger.INSTANCE;

    // Singleton instance
    private static GameManager instance;

    // Queue for waiting players
    private final ConcurrentLinkedQueue<ClientHandler> waitingPlayers;

    // List of active game sessions
    private final List<GameSession> activeSessions;

    // Thread pool for running game sessions
    private final ExecutorService gameExecutor;

    /**
     * Private constructor for singleton pattern.
     */
    private GameManager() {
        this.waitingPlayers = new ConcurrentLinkedQueue<>();
        this.activeSessions = Collections.synchronizedList(new ArrayList<>());

        // Create a thread pool with a reasonable number of threads
        // For 2000 concurrent players (1000 games), a smaller pool is still efficient
        this.gameExecutor = Executors.newFixedThreadPool(100);

        // Start the matchmaking thread
        Thread matchmakingThread = new Thread(this::performMatchmaking, "MatchmakingThread");
        matchmakingThread.setDaemon(true);
        matchmakingThread.start();

        // Start the session cleanup thread
        Thread cleanupThread = new Thread(this::cleanupInactiveSessions, "SessionCleanupThread");
        cleanupThread.setDaemon(true);
        cleanupThread.start();

        logger.info(Constants.LOG_GAME_MANAGER_STARTED);
    }

    /**
     * Gets the singleton instance of the GameManager.
     *
     * @return The GameManager instance
     */
    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    /**
     * Adds a player to the waiting queue.
     *
     * @param clientHandler The client handler for the player to add
     */
    public void addWaitingPlayer(ClientHandler clientHandler) {
        waitingPlayers.add(clientHandler);
        clientHandler.sendMessage(Constants.MSG_WAITING_FOR_OPPONENT);
        logger.info(String.format(Constants.LOG_PLAYER_WAITING, clientHandler.getClientInfo()));
    }

    /**
     * Removes a player from the waiting queue.
     *
     * @param clientHandler The client handler for the player to remove
     * @return true if the player was in the queue, false otherwise
     */
    public boolean removeWaitingPlayer(ClientHandler clientHandler) {
        boolean removed = waitingPlayers.remove(clientHandler);
        if (removed) {
            logger.info(String.format(Constants.LOG_PLAYER_LEFT_QUEUE, clientHandler.getClientInfo()));
        }
        return removed;
    }

    /**
     * Continuously matches waiting players and creates game sessions.
     */
    private void performMatchmaking() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (waitingPlayers.size() >= 2) {
                    ClientHandler player1 = waitingPlayers.poll();

                    // Check if player1 is still connected
                    if (player1 != null && player1.isConnected()) {
                        ClientHandler player2 = waitingPlayers.poll();

                        // Check if player2 is still connected
                        if (player2 != null && player2.isConnected()) {
                            createGameSession(player1, player2);
                        } else if (player1.isConnected()) {
                            // Put player1 back in the queue if player2 disconnected
                            waitingPlayers.add(player1);
                        }
                    }
                }

                // Sleep to prevent CPU hogging
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warning(String.format(Constants.ERROR_MATCHMAKING_INTERRUPTED, e.getMessage()));
            } catch (Exception e) {
                logger.severe(String.format(Constants.ERROR_MATCHMAKING_FAILURE, e.getMessage()));
            }
        }
    }

    /**
     * Creates a new game session between two players and submits it to the executor.
     *
     * @param player1 The first player
     * @param player2 The second player
     */
    private void createGameSession(ClientHandler player1, ClientHandler player2) {
        GameSession session = new GameSession(player1, player2);
        activeSessions.add(session);

        // Submit the game session to the thread pool
        gameExecutor.submit(() -> {
            try {
                session.play();

                // After game ends, check if players want to play again
                handlePlayAgainRequests(player1, player2);
            } catch (Exception e) {
                logger.severe(String.format(Constants.ERROR_GAME_SESSION_FAILURE,
                        player1.getClientInfo(), player2.getClientInfo(), e.getMessage()));
            }
        });
    }

    /**
     * Handles requests from players to play again after a game ends.
     *
     * @param player1 The first player
     * @param player2 The second player
     */
    private void handlePlayAgainRequests(ClientHandler player1, ClientHandler player2) {
        try {
            // Add connected players who want to play again back to the waiting queue
            if (player1.isConnected()) {
                String response1 = player1.observeMessage();
                if (response1 != null && response1.equalsIgnoreCase(Constants.RESP_YES)) {
                    addWaitingPlayer(player1);
                }
            }

            if (player2.isConnected()) {
                String response2 = player2.observeMessage();
                if (response2 != null && response2.equalsIgnoreCase(Constants.RESP_YES)) {
                    addWaitingPlayer(player2);
                }
            }
        } catch (Exception e) {
            logger.warning(String.format(Constants.ERROR_PLAY_AGAIN_HANDLING, e.getMessage()));
        }
    }

    /**
     * Periodically removes inactive game sessions from the list.
     */
    private void cleanupInactiveSessions() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                synchronized (activeSessions) {
                    activeSessions.removeIf(session -> !session.isActive());
                }

                // Sleep to prevent CPU hogging
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warning(String.format(Constants.ERROR_CLEANUP_INTERRUPTED, e.getMessage()));
            } catch (Exception e) {
                logger.severe(String.format(Constants.ERROR_CLEANUP_FAILURE, e.getMessage()));
            }
        }
    }

    /**
     * Gets the number of players currently waiting for a game.
     *
     * @return The number of waiting players
     */
    public int getWaitingPlayersCount() {
        return waitingPlayers.size();
    }

    /**
     * Gets the number of active game sessions.
     *
     * @return The number of active sessions
     */
    public int getActiveSessionsCount() {
        synchronized (activeSessions) {
            return activeSessions.size();
        }
    }

    /**
     * Shuts down the game manager and its resources.
     */
    public void shutdown() {
        gameExecutor.shutdownNow();
        logger.info(Constants.LOG_GAME_MANAGER_SHUTDOWN);
    }
}