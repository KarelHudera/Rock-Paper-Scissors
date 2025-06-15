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

    //logger
    private final Logger logger;
    //soket
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String loggedInUsername = null;

    /**
     * Konstruktor
     */
    public Client(Logger logger) {
        this.logger = logger;
    }
    private volatile boolean connected = false;

    /**
     * Kontrola připojení klienta.
     */
    public boolean isConnected() {
        return connected && (socket != null && !socket.isClosed() && socket.isConnected());
    }


    /**
     * Naváže spojení se serverem na předem definované adrese a portu.
     * Spustí I/O stream.
     */
    private void connect() throws IOException {
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            logger.info("Attempting to connect to server at " + Constants.SERVER_ADDRESS + ":" + Constants.PORT);
            socket = new Socket(Constants.SERVER_ADDRESS, Constants.PORT);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            logger.info("Successfully connected to server and initialized streams.");
            this.connected = true;
        } else {
            logger.info("Already connected or socket is still active.");
        }
    }

    /**
     * Odesílá 'LoginRequest' na server a očekává 'LoginResponse'
     *
     * Parametry:
     *    - username: Uživatelské jméno pro autentizaci.
     *    - password: Heslo pro autentizaci (aktuálně se neposílá na server v LoginRequest).
     *
     * Vrátí:
     *    - boolean: 'true', pokud je autentizace úspěšná; jinak 'false'.
     * **/
    public boolean authenticate(String username, String password) {
        try {
            connect();

            LoginRequest request = new LoginRequest(username, password);
            logger.info(Constants.LOG_AUTH_ATTEMPT + username);
            output.writeObject(request);
            output.flush();

            Object responseObj = input.readObject();

            if (responseObj instanceof LoginResponse) {
                LoginResponse response = (LoginResponse) responseObj;
                if (response.isSuccess()) {
                    this.loggedInUsername = username;
                    logger.info(Constants.LOG_AUTH_SUCCESS + username);
                    return true;
                } else {
                    logger.warning(Constants.LOG_AUTH_FAIL + username + ": " + response.getMessage());
                    return false;
                }
            } else {
                logger.log(Level.SEVERE, Constants.ERROR_LOGIN_FAILED + " Received unexpected object type during login: " + responseObj.getClass().getName());
                closeConnection();
                return false;
            }

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, Constants.ERROR_LOGIN_FAILED + e.getMessage(), e);
            closeConnection();
            return false;
        }
    }


    /**
     * Přečte jeden objekt (očekává se 'GameMessage') ze vstupního proudu serveru.
     * Loguje typ přijaté zprávy.
     * Vrací přijatou zprávu nebo null.
     * **/
    public GameMessage readServerMessage() throws IOException, ClassNotFoundException {
        if (input != null) {
            Object obj = input.readObject();
            if (obj instanceof GameMessage) {
                logger.info(String.format(Constants.LOG_RECEIVED_MESSAGE + " %s", obj.getClass().getSimpleName()));
                return (GameMessage) obj;
            } else {
                logger.warning(String.format(Constants.LOG_RECEIVED_MESSAGE + " Unexpected type: %s", obj.getClass().getName()));
                return null;
            }
        }
        return null;
    }

    /**
     * Odešle objekt 'GameMessage' na server prostřednictvím výstupního proudu.
     * Zaloguje informace o odeslané zprávě.
     */
    public void sendToServer(GameMessage message) throws IOException {
        if (output != null) {
            logger.info(String.format("📤 Client sent message to server: %s - %s", message.getClass().getSimpleName(), message.toString()));
            output.writeObject(message);
            output.flush();
        } else {
            logger.warning("Attempted to send message but output stream is null.");
        }
    }


    /**
     * Bezpečně uzavře klientské spojení se serverem a uvolní všechny související sockety a proudy.
     * Psílá zprávu o ukončení spojení serveru.
     */
    public void closeConnection() {
        try {
            if (output != null && socket != null && !socket.isClosed()) {
                try {
                    TerminateMessage terminateMessage = new TerminateMessage();
                    output.writeObject(terminateMessage);
                    output.flush();
                    logger.info("Sent TerminateMessage to server.");
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Failed to send TerminateMessage before closing: " + e.getMessage());
                }
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
                logger.info("Socket closed.");
            }
            if (input != null) {
                input.close();
                logger.info("Input stream closed.");
            }
            if (output != null) {
                output.close();
                logger.info("Output stream closed.");
            }

            logger.info(Constants.LOG_CLIENT_CLOSED);
            this.connected = false;

        } catch (IOException e) {
            logger.log(Level.SEVERE, Constants.LOG_CLIENT_CLOSE_ERROR + ": " + e.getMessage(), e);
        } finally {
            socket = null;
            input = null;
            output = null;
            loggedInUsername = null;
        }
    }
}