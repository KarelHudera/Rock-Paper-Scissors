package karel.hudera.rps.server;

import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.utils.ServerLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * The {@code Server} component for the Rock-Paper-Scissors game.
 * <p>
 * This class manages the server socket, accepts client connections, and
 * creates client handlers for each connected client. It also tracks active
 * connections and logs server activity.
 * </p>
 * <p>
 * The server runs in a separate thread to monitor connection status while
 * the main thread accepts new connections.
 * </p>
 */
public class Server implements Runnable {

    private static final Logger logger = ServerLogger.INSTANCE;

    private ServerSocket serverSocket;
    private int portNumber;
    private List<Thread> connections;
    private volatile boolean isRunning;

    public Server(int portNumber) {
        this.portNumber = portNumber;
        this.connections = Collections.synchronizedList(new ArrayList<Thread>());
        this.isRunning = true;

        Thread monitorThread = new Thread(this, "ConnectionMonitor");
        monitorThread.start();
    }

    /**
     * Initializes the server and begins accepting client connections.
     * <p>
     * This method configures logging, creates a server socket, and enters a loop
     * to accept and handle client connections. For each new connection, a {@link ClientHandler}
     * is created and started in a new thread.
     * </p>
     */
    public void initialize() {
        try {
            serverSocket = new ServerSocket(portNumber);
            logger.info(Constants.LOG_SERVER_RUNNING);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread thread = new Thread(clientHandler);
                connections.add(thread);
                thread.start();
            }
        } catch (Exception e) {
            logger.severe(String.format(Constants.ERROR_LOGIN_FAILED, e.getMessage()));
        } finally {
            logger.info(Constants.LOG_SERVER_STOPPED);
            shutdown();
        }
    }

    /**
     * Shuts down the server safely.
     */
    private void shutdown() {
        isRunning = false;
        closeServerSocket();
    }

    /**
     * Safely closes the server socket if it exists and is not already closed.
     */
    private void closeServerSocket() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                logger.info(Constants.LOG_SERVER_STOPPED);
            } catch (IOException e) {
                logger.severe(String.format(Constants.ERROR_SERVER_SOCKET_CLOSE_FAILED, e.getMessage()));
            }
        }
    }

    /**
     * Monitors the active client connections and logs changes in the connection count.
     * <p>
     * This method runs in a separate thread and periodically checks the number of active
     * connections, logging changes when they occur.
     * </p>
     */
    @Override
    public void run() {
        int previousCount = 0;

        while (isRunning) {
            int currentCount = 0;

            synchronized (connections) {
                for (Thread thread : connections) {
                    if (thread.isAlive()) {
                        currentCount++;
                    }
                }
            }

            if (currentCount != previousCount) {
                if (currentCount == 0)
                    logger.info(Constants.LOG_NO_USERS_CONNECTED);
                else
                    logger.info(String.format(Constants.LOG_USERS_CONNECTED, currentCount));
            }

            previousCount = currentCount;

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Preserve interrupt status
                logger.severe(e.getMessage());
            }
        }
    }
}