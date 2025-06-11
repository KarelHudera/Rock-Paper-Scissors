package karel.hudera.rps.server;

import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.game.GameManager;
import karel.hudera.rps.game.GameState;
import karel.hudera.rps.game.LoginResponse;
import karel.hudera.rps.utils.ServerLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/**
 * Handles communication between the server and a connected client in the Rock-Paper-Scissors game.
 * Each client connection is managed in a separate thread to allow multiple concurrent connections.
 * This class is responsible for:
 * <ul>
 *   <li>Establishing input/output streams with the client</li>
 *   <li>Processing incoming client messages</li>
 *   <li>Sending responses back to the client</li>
 *   <li>Logging all client activity</li>
 *   <li>Properly closing resources when the connection terminates</li>
 *   <li>Registering the client with the GameManager for matchmaking</li>
 * </ul>
 *
 * @author Karel Hudera
 */
public class ClientHandler implements Runnable {

    private static final Logger logger = ServerLogger.INSTANCE;
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private ObjectOutputStream objectOut;
    private ObjectInputStream objectIn;
    private volatile boolean connected;

    /**
     * Constructs a new ClientHandler to manage communication with a connected client.
     *
     * @param clientSocket The socket through which the client communicates with the server.
     */
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.connected = true;
    }

    /**
     * Executes the client handling logic in a separate thread.
     * This method initializes streams, processes client messages, and handles connection closure.
     * All activities are logged to both console and file according to the logging configuration.
     */
    @Override
    public void run() {
        String clientAddress = clientSocket.getInetAddress().toString();
        int clientPort = clientSocket.getPort();
        logger.info(String.format(Constants.LOG_CLIENT_CONNECTED, clientAddress, clientPort));

        try {
            // Initialize input and output streams
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Initialize object streams for serialized communication
            objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
            objectIn = new ObjectInputStream(clientSocket.getInputStream());


            GameState welcomeState = new GameState(GameState.GameStatus.WAITING_FOR_PLAYERS, Constants.WELCOME_MESSAGE);
            objectOut.writeObject(welcomeState);
            objectOut.flush(); // Důležité: Vždy po odeslání objektu stream vyprázdněte (flush)!

            // Send welcome message as LoginResponse object
            LoginResponse welcomeResponse = new LoginResponse(true, Constants.WELCOME_MESSAGE);
            sendObject(welcomeResponse);
            logger.info(String.format(Constants.LOG_WELCOME_SENT, clientAddress, clientPort));

            // Add player to waiting queue
            GameManager.getInstance().addWaitingPlayer(this);

            // Keep connection alive until client disconnects
            while (connected && !clientSocket.isClosed()) {
                try {
                    // Socket will be monitored for input from the GameSession
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warning("Client handler thread interrupted: " + e.getMessage());
                    break;
                }
            }
        } catch (IOException e) {
            logger.warning(String.format(Constants.ERROR_CLIENT_COMMUNICATION, clientAddress, clientPort, e.getMessage()));
        } finally {
            // Remove from waiting queue if still there
            GameManager.getInstance().removeWaitingPlayer(this);
            closeConnection(clientAddress, clientPort);
        }
    }

    /**
     * Closes all resources associated with this client connection.
     *
     * @param clientAddress The client's IP address
     * @param clientPort    The client's port number
     */
    private void closeConnection(String clientAddress, int clientPort) {
        try {
            connected = false;

            // Close resources
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();

            logger.info(String.format(Constants.LOG_CLIENT_DISCONNECTED, clientAddress, clientPort));
        } catch (IOException e) {
            logger.severe(String.format(Constants.ERROR_CLOSING_CONNECTION, clientAddress, clientPort, e.getMessage()));
        }
    }

    /**
     * Sends a message to the client.
     *
     * @param message The message to send to the client.
     */
    public void sendMessage(String message) {
        if (out != null && isConnected()) {
            out.println(message);
            logger.info(String.format(Constants.LOG_SENT_TO_CLIENT,
                    clientSocket.getInetAddress(), clientSocket.getPort(), message));
        } else {
            logger.warning(String.format(Constants.LOG_FAILED_SEND,
                    clientSocket.getInetAddress(), clientSocket.getPort()));
        }
    }

    /**
     * Sends a serialized object to the client.
     *
     * @param obj The object to send to the client.
     */
    public void sendObject(Object obj) {
        if (objectOut != null && isConnected()) {
            try {
                objectOut.writeObject(obj);
                objectOut.flush();
                logger.info(String.format(Constants.LOG_SENT_TO_CLIENT,
                        clientSocket.getInetAddress(), clientSocket.getPort(), obj.toString()));
            } catch (IOException e) {
                logger.warning(String.format("Failed to send object to client %s:%d - %s",
                        clientSocket.getInetAddress(), clientSocket.getPort(), e.getMessage()));
            }
        } else {
            logger.warning(String.format(Constants.LOG_FAILED_SEND,
                    clientSocket.getInetAddress(), clientSocket.getPort()));
        }
    }

    /**
     * Sends a LoginResponse to the client.
     *
     * @param success Whether the login was successful
     * @param message The message to send with the response
     */
    public void sendLoginResponse(boolean success, String message) {
        LoginResponse response = new LoginResponse(success, message);
        sendObject(response);
    }

    /**
     * Observes and receives a message from the client.
     *
     * @return The message received from the client or null if reading failed
     * @throws IOException If an I/O error occurs when reading
     */
    public String observeMessage() throws IOException {
        if (in != null && isConnected()) {
            String message = in.readLine();
            if (message != null) {
                logger.info(String.format(Constants.LOG_RECEIVED_FROM_CLIENT,
                        clientSocket.getInetAddress(), clientSocket.getPort(), message));
            } else {
                connected = false;
            }
            return message;
        }
        return null;
    }

    /**
     * Gets a string representation of the client's address and port.
     *
     * @return A string in the format "address:port"
     */
    public String getClientInfo() {
        return clientSocket.getInetAddress() + ":" + clientSocket.getPort();
    }

    /**
     * Checks if the client is still connected.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected && !clientSocket.isClosed();
    }
}