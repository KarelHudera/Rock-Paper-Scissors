package karel.hudera.rps.server;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

import karel.hudera.rps.constants.Constants;


/**
 * Handles communication between the server and a connected client.
 * Each client runs in a separate thread.
 */
class ClientHandler implements Runnable {

    /**
     * Logger instance for logging client activity
     */
    private static Logger logger;
    /**
     * Socket instance representing the client's connection to the server
     */
    private Socket socket;

    /**
     * Input stream to read objects sent by the client
     */
    ObjectInputStream input;

    /**
     * Output stream to send objects to the client
     */
    private ObjectOutputStream output;

    /**
     * The username of the connected client
     */
    String username;

    /**
     * Constructs a new {@code ClientHandler} to manage communication with the connected client.
     *
     * @param socket The socket through which the client communicates with the server.
     * @param logger The logger instance used for logging client activity.
     */
    public ClientHandler(Socket socket, Logger logger) {
        this.socket = socket;
        ClientHandler.logger = logger;
    }

    /**
     * Runs the client handler in a separate thread.
     * The method listens for incoming client messages, handles authentication,
     * and starts a game session once the client is authenticated and ready.
     */
    @Override
    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            while (true) {
                username = (String) input.readObject();
                if (Server.addUser(username)) {
                    output.writeObject(Constants.OK);
                    logger.info(Constants.LOG_AUTH_SUCCESS + username);
                    break;
                } else {
                    output.writeObject(Constants.USERNAME_TAKEN);
                    logger.warning(Constants.LOG_USERNAME_TAKEN + username);
                }
            }

            logger.info(String.format(Constants.LOG_CLIENT_CONNECTED, username));
            Server.addWaitingPlayer(this);
        } catch (IOException | ClassNotFoundException e) {
            logger.severe(Constants.LOG_CLIENT_ERROR + e.getMessage());
        } finally {
            Server.removeUser(username);
        }
    }

    /**
     * Sends a message to the client.
     *
     * @param message The message to send to the client.
     * @throws IOException If an I/O error occurs while sending the message.
     */
    public void sendMessage(Object message) throws IOException {
        output.writeObject(message);
        logger.info(Constants.LOG_MOVE_SENT + message);
    }

    /**
     * Closes the input and output streams and the client socket to clean up resources.
     */
    public void closeResources() {
        try {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (socket != null) {
                socket.close();
            }
            logger.info(Constants.LOG_CLIENT_CLOSED + username);
        } catch (IOException e) {
            logger.severe(Constants.LOG_CLIENT_CLOSE_ERROR + e.getMessage());
        }
    }
}