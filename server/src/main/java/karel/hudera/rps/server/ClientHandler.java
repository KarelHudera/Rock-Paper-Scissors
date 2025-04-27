package karel.hudera.rps.server;

import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.utils.ServerLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

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
 * </ul>
 */
public class ClientHandler implements Runnable {

    private static final Logger logger = ServerLogger.INSTANCE;
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Constructs a new ClientHandler to manage communication with a connected client.
     *
     * @param clientSocket The socket through which the client communicates with the server.
     */
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
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

            // Send welcome message
            out.println(Constants.WELCOME_MESSAGE);
            logger.info(String.format(Constants.LOG_WELCOME_SENT, clientAddress, clientPort));

            // Process client messages
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                logger.info(String.format(Constants.LOG_RECEIVED_FROM_CLIENT, clientAddress, clientPort, inputLine));

                // Echo the message back to client
                String outputLine = Constants.SERVER_ECHO + inputLine;
                out.println(outputLine);
                logger.info(String.format(Constants.LOG_SENT_TO_CLIENT, clientAddress, clientPort, outputLine));
            }
        } catch (IOException e) {
            logger.warning(String.format(Constants.ERROR_CLIENT_COMMUNICATION, clientAddress, clientPort, e.getMessage()));
        } finally {
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
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean sendMessage(String message) {
        if (out != null && !clientSocket.isClosed()) {
            out.println(message);
            logger.info(String.format(Constants.LOG_SENT_TO_CLIENT,
                    clientSocket.getInetAddress(), clientSocket.getPort(), message));
            return true;
        }
        return false;
    }
}