package karel.hudera.rps.server;

import karel.hudera.rps.constants.Constants;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
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
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();
    private static final BlockingQueue<ClientHandler> waitingPlayers = new LinkedBlockingQueue<>();
    private static final Set<String> activeUsers = Collections.synchronizedSet(new HashSet<>());

    /**
     * Constructs a new Server instance.
     *
     * @param logger The logger instance for logging server activity.
     */
    public Server(Logger logger) {
        Server.logger = logger;
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
}