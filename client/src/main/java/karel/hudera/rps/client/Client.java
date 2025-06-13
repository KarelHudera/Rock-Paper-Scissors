package karel.hudera.rps.client;

import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.game.*;
import karel.hudera.rps.utils.UserCredentials;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the client that connects to the Rock-Paper-Scissors server.
 * <p>
 * The client handles user authentication, game interactions, and communication
 * with the server over a socket connection. Users input their username and
 * choose their moves to play against an opponent.
 * </p>
 * <p>
 * The client follows this process:
 * <ol>
 *     <li>Connects to the server.</li>
 *     <li>Authenticates with a unique username.</li>
 *     <li>Waits for an opponent.</li>
 *     <li>Plays rounds of Rock-Paper-Scissors.</li>
 *     <li>Receives game results from the server.</li>
 * </ol>
 * </p>
 *
 * @author Karel Hudera
 */
public class Client {

    /**
     * Logger instance for logging client activity
     */
    private final Logger logger;

    /**
     * The socket used for communication with the server.
     */
    private Socket socket;

    /**
     * Stream for sending objects to the server.
     */
    private ObjectOutputStream output;

    /**
     * Stream for receiving objects from the server.
     */
    private ObjectInputStream input;
    private String loggedInUsername = null;

    /**
     * Constructs a new Client instance with a specified logger.
     *
     * @param logger The logger instance configured for logging to a file.
     */
    public Client(Logger logger) {
        this.logger = logger;
    }
    private volatile boolean connected = false;

    /**
     * Public method to check if the client is currently connected.
     * This method was missing!
     * @return true if connected, false otherwise.
     */
    public boolean isConnected() {
        return connected && (socket != null && !socket.isClosed() && socket.isConnected());
    }


    /**
     * Initializes the client connection to the specified server and port.
     * <p>
     * The client establishes a connection, prompts the user for a username,
     * and continuously plays rounds of Rock-Paper-Scissors by sending moves
     * and receiving results from the server.
     * </p>
     *
     * @param serverAddress The address of the server to connect to.
     * @param port          The port number on which the server is running.
     */

    /**
     * Establishes a connection to the server and initializes object streams.
     * This method should be called before attempting any communication.
     * If already connected, it does nothing.
     *
     * @throws IOException If an I/O error occurs when creating the socket or streams.
     */
    private void connect() throws IOException {
        if (socket == null || socket.isClosed() || !socket.isConnected()) { // Přidána kontrola !socket.isConnected()
            logger.info("Attempting to connect to server at " + Constants.SERVER_ADDRESS + ":" + Constants.PORT);
            socket = new Socket(Constants.SERVER_ADDRESS, Constants.PORT);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            logger.info("Successfully connected to server and initialized streams.");
            this.connected = true; // *** DŮLEŽITÉ: Nastavte na true po úspěšném připojení! ***
        } else {
            logger.info("Already connected or socket is still active.");
        }
    }


    public boolean authenticate(String username, String password) {
        try {
            connect(); // Zajistíme, že jsme připojeni k serveru

            // Vytvoříme a pošleme LoginRequest objekt
            LoginRequest request = new LoginRequest(username);
            logger.info(Constants.LOG_AUTH_ATTEMPT + username); // Používáme vaši konstantu
            output.writeObject(request);
            output.flush(); // Důležité pro okamžité odeslání dat

            // Přečteme odpověď od serveru
            Object responseObj = input.readObject();

            if (responseObj instanceof LoginResponse) {
                LoginResponse response = (LoginResponse) responseObj;
                if (response.isSuccess()) {
                    this.loggedInUsername = username; // Uložíme přihlášené jméno
                    logger.info(Constants.LOG_AUTH_SUCCESS + username); // Používáme vaši konstantu
                    return true;
                } else {
                    logger.warning(Constants.LOG_AUTH_FAIL + username + ": " + response.getMessage()); // Používáme vaši konstantu
                    // Serverová zpráva již obsahuje důvod selhání.
                    // V tomto případě spojení nezavírám, aby mohl uživatel zkusit znovu přihlášení.
                    return false;
                }
            } else {
                logger.log(Level.SEVERE, Constants.ERROR_LOGIN_FAILED + " Received unexpected object type during login: " + responseObj.getClass().getName()); // Používáme vaši konstantu
                closeConnection(); // V případě neočekávané zprávy bychom měli spojení ukončit
                return false;
            }

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, Constants.ERROR_LOGIN_FAILED + e.getMessage(), e); // Používáme vaši konstantu
            closeConnection(); // Při chybě spojení zavřeme
            return false;
        }
    }
    public GameMessage readServerMessage() throws IOException, ClassNotFoundException {
        if (input != null) {
            Object obj = input.readObject();
            if (obj instanceof GameMessage) {
                logger.info(String.format(Constants.LOG_RECEIVED_MESSAGE + " %s", obj.getClass().getSimpleName())); // Používáme vaši konstantu
                return (GameMessage) obj;
            } else {
                logger.warning(String.format(Constants.LOG_RECEIVED_MESSAGE + " Unexpected type: %s", obj.getClass().getName())); // Používáme vaši konstantu
                return null;
            }
        }
        return null;
    }

    /**
     * Sends a GameMessage object to the server.
     *
     * @param message The GameMessage object to send.
     * @throws IOException If an I/O error occurs during writing.
     */
    public void sendToServer(GameMessage message) throws IOException {
        if (output != null) {
            logger.info(String.format("📤 Client sent message to server: %s - %s", message.getClass().getSimpleName(), message.toString())); // Používáme vaši konstantu, ale pro klienta je to vlastně "odesláno na server"
            output.writeObject(message);
            output.flush();
        } else {
            logger.warning("Attempted to send message but output stream is null.");
        }
    }

    /**
     * Returns the username of the currently logged-in user.
     *
     * @return The logged-in username, or null if no user is logged in.
     */
    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    /**
     * Closes the client connection and releases resources.
     * <p>
     * Ensures the socket and streams are properly closed to avoid resource leaks.
     * </p>
     */
    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            logger.info(Constants.LOG_CLIENT_CLOSED); // Používáme vaši konstantu
            this.connected = false;
        } catch (IOException e) {
            logger.log(Level.SEVERE, Constants.LOG_CLIENT_CLOSE_ERROR + ": " + e.getMessage(), e); // Používáme vaši konstantu
        } finally {
            socket = null;
            input = null;
            output = null;
            loggedInUsername = null;
        }
    }
}