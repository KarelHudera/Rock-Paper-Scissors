package karel.hudera.rps.server;

import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.game.GameSession;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * The {@code Server} class represents the game server for Rock-Paper-Scissors.
 * It manages client connections, authentication, and multithreaded request handling.
 * <p>
 * Uses an {@link ExecutorService} to limit the number of concurrent client connections.
 * </p>
 */
public class Server {

    /**
     * Logger instance for logging server activity
     */
    private static Logger logger;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(Constants.MAX_THREADS);
    private static final BlockingQueue<ClientHandler> waitingPlayers = new LinkedBlockingQueue<>();
    private static final Set<String> activeUsers = Collections.synchronizedSet(new HashSet<>());


    private static final Map<String, String> allowedUsers = new HashMap<>();

    /**
     * Constructs a new Server instance.
     *
     * @param logger The logger instance for logging server activity.
     */
    public Server(Logger logger) {
        Server.logger = logger;
        loadUsers();
    }

    /**
     * Initializes the server and listens for client connections on the specified port.
     * Once a client connects, a new {@link ClientHandler} is created to handle the connection.
     *
     * <p>The server continues to accept connections and assigns each new connection to a thread from the thread pool.</p>
     *
     * @param port The port on which the server listens for incoming connections.
     */
    public void initialize(int port) {
        logger.info(Constants.LOG_SERVER_RUNNING);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, logger);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            logger.severe(Constants.LOG_CLIENT_ERROR + e.getMessage());
        } finally {
            logger.info(Constants.LOG_SERVER_STOPPED);
            threadPool.shutdown();
        }
    }

    /**
     * Adds a player to the waiting queue or starts a new game session if an opponent is available.
     *
     * @param player The {@link ClientHandler} representing the player to add to the waiting queue.
     */
    public static void addWaitingPlayer(ClientHandler player) {
        try {
            if (!waitingPlayers.isEmpty()) {
                ClientHandler opponent = waitingPlayers.take();
                new GameSession(player, opponent, logger).start();
            } else {
                waitingPlayers.put(player);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Attempts to add a new user to the active users set.
     *
     * @param username The username to add.
     * @return {@code true} if the user was successfully added, {@code false} if the username is already taken.
     */
    public static boolean addUser(String username) {
        boolean success = activeUsers.add(username);
        if (success) {
            logger.info(Constants.LOG_AUTH_SUCCESS + username);
        } else {
            logger.warning(Constants.LOG_USERNAME_TAKEN + username);
        }
        return success;
    }

    /**
     * Removes a user from the active users set.
     *
     * @param username The username to remove.
     */
    public static void removeUser(String username) {
        activeUsers.remove(username);
        logger.info(Constants.LOG_CLIENT_CLOSED + username);
    }

    public static boolean isUserLoggedIn(String username) {
        return activeUsers.contains(username);
    }


    /**
     * Validates the username and password against the allowed users.
     *
     * @param username The username to check.
     * @param password The password to verify.
     * @return {@code true} if the credentials are valid, otherwise {@code false}.
     */
    public static boolean isValidUser(String username, String password) {
        logger.info(username + " " + password);
        return allowedUsers.containsKey(username) && allowedUsers.get(username).equals(password);
    }

    /**
     * Loads valid users. In production, this should be from a database or configuration file.
     */
    private void loadUsers() {
        allowedUsers.put("karel", "pass");
        allowedUsers.put("admin", "adminpass");
        allowedUsers.put("player1", "rpsgame");

        logger.info(Constants.LOG_USERS_LOADED + allowedUsers.keySet());
    }
}