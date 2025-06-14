package karel.hudera.rps.server;

import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.game.*;
import karel.hudera.rps.utils.ServerLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
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
    private ObjectOutputStream objectOut;
    private ObjectInputStream objectIn;
    private volatile boolean connected;
    private String username;
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
            // Initialize object streams for serialized communication
            objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
            objectIn = new ObjectInputStream(clientSocket.getInputStream());

            Object initialMessage = objectIn.readObject(); // <-- Server čeká na PRVNÍ zprávu, která by měla být LoginRequest
            if (!(initialMessage instanceof LoginRequest)) {
                logger.warning("Received unexpected first message from client: " + initialMessage.getClass().getName());
                // Můžeš poslat LoginResponse(false, "Unexpected initial message")
                sendMessage(new LoginResponse(false, "Niočekávaná první zpráva."));
                return; // Ukončit zpracování pro tohoto klienta
            }

            LoginRequest loginRequest = (LoginRequest) initialMessage;
            this.username = loginRequest.getUsername();
            logger.info("Received LOGIN request from " + username + " at " + getClientInfo());

            // PROTOKOL: Server posílá LOGIN_RESPONSE
            LoginResponse loginResponse = new LoginResponse(true, "Connected to RPS server"); // Můžeš použít Constants.WELCOME_MESSAGE
            sendMessage(loginResponse);
            logger.info("Sent LOGIN_RESPONSE to " + getClientInfo());

            // Add player to waiting queue
            GameManager.getInstance().addWaitingPlayer(this);
            logger.info(String.format(Constants.LOG_PLAYER_WAITING, getClientInfo()));

            // Keep connection alive until client disconnects
            while (connected && !clientSocket.isClosed()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warning("Client handler thread interrupted: " + e.getMessage());
                    break;
                }
            }
        } catch (IOException e) {
            logger.warning(String.format(Constants.ERROR_CLIENT_COMMUNICATION, clientAddress, clientPort, e.getMessage()));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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
            if (objectIn != null) objectIn.close();
            if (objectOut != null) objectOut.close();
            if (clientSocket != null) clientSocket.close();

            logger.info(String.format(Constants.LOG_CLIENT_DISCONNECTED, clientAddress, clientPort));
        } catch (IOException e) {
            logger.severe(String.format(Constants.ERROR_CLOSING_CONNECTION, clientAddress, clientPort, e.getMessage()));
        }
    }

    /**
     * Sends a GameMessage object to the client.
     *
     * @param message The GameMessage object to send to the client.
     */
    public void sendMessage(GameMessage message) {
        if (objectOut != null && isConnected()) {
            try {
                objectOut.writeObject(message);
                objectOut.flush();
                logger.info(String.format(Constants.LOG_SENT_TO_CLIENT,
                        clientSocket.getInetAddress(), clientSocket.getPort(), message.toString()));
            } catch (IOException e) {
                logger.warning(String.format("Failed to send message to client %s:%d - %s",
                        clientSocket.getInetAddress(), clientSocket.getPort(), e.getMessage()));
                connected = false;
            }
        } else {
            logger.warning(String.format(Constants.LOG_FAILED_SEND,
                    clientSocket.getInetAddress(), clientSocket.getPort()));
        }
    }

    /**
     * Observes and receives a message from the client.
     * Can handle both GameMessage objects and String messages.
     *
     * @return The message received from the client or null if reading failed
     * @throws IOException If an I/O error occurs when reading
     */
    public GameMessage observeMessage() throws IOException {
        if (objectIn != null && isConnected()) {
            try {
                // Try to read as an object first (for GameMessage instances)
                GameMessage message = (GameMessage) objectIn.readObject();

                if (message != null) {
                    logger.info(String.format(Constants.LOG_RECEIVED_FROM_CLIENT,
                            clientSocket.getInetAddress(), clientSocket.getPort(),
                            message));
                    return message;
                } else {
                    connected = false;
                    return null;
                }
            } catch (ClassNotFoundException e) {
                logger.warning(String.format("Failed to deserialize object from client %s:%d - %s",
                        clientSocket.getInetAddress(), clientSocket.getPort(), e.getMessage()));
                connected = false;
                return null;
            } catch (IOException e) {
                logger.warning(String.format("I/O error reading from client %s:%d - %s",
                        clientSocket.getInetAddress(), clientSocket.getPort(), e.getMessage()));
                connected = false;
                throw e;
            }
        }
        return null;
    }

    /**
     * Gets a string representation of the client's address and port.
     *
     * @return A string in the format "address:port"
     */
    public String getClientInfo() {
        if (clientSocket != null) {
            try {
                return clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
            } catch (Exception e) {
                logger.log(Level.FINE, "Failed to get socket info for client: " + e.getMessage());
                return username != null ? username + " (Socket Error)" : "Unknown Client (Socket Error)";
            }
        } else if (username != null) { // Fallback pokud socket je null
            return username + " (No Socket)";
        } else {
            return "Unknown Client";
        }
    }

    /**
     * Checks if the client is still connected.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected && !clientSocket.isClosed();
    }

    public String getUsername() {
        return this.username;
    }
}