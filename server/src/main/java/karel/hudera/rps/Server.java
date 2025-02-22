package karel.hudera.rps;

import karel.hudera.rps.utils.Logging;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code Server} class represents the game server for Rock-Paper-Scissors.
 * It manages client connections, authentication, and multithreaded request handling.
 * <p>
 * Uses an {@link ExecutorService} to limit the number of concurrent client connections.
 * </p>
 */
public class Server {
    private static final int PORT = 9090;
    private static final int MAX_THREADS = 3;

    // Logger instance for server logging
    static final Logger logger = Logger.getLogger(Server.class.getName());

    // Stores active users (username -> authentication token)
    private final Map<String, String> activeUsers = new ConcurrentHashMap<>();
    private final Map<String, String> validUsers = Map.of(
            "kar", "1234"
    );

    private final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);

    /**
     * Entry point of the server application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        // Configure logging for the server
        Logging.configureLogger(logger, "server.log");

        // Start the server
        new Server().start();
    }

    /**
     * Starts the game server, listens for client connections, and assigns them to the thread pool.
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("New connection from: " + clientSocket.getRemoteSocketAddress());

                // Submit client handling task to the thread pool
                threadPool.execute(new ClientHandler(clientSocket, activeUsers, validUsers));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server encountered an error", e);
        } finally {
            shutdownServer();
        }
    }

    /**
     * Gracefully shuts down the server by terminating the thread pool.
     */
    private void shutdownServer() {
        logger.info("Shutting down server...");
        threadPool.shutdown();
    }
}