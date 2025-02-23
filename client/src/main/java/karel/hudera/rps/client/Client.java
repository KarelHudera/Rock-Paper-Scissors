package karel.hudera.rps.client;

import karel.hudera.rps.StartClient;
import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.game.GameResult;
import karel.hudera.rps.game.Move;

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

    /** Logger instance for logging client activity */
    private final Logger logger;

    /** The socket used for communication with the server. */
    private Socket socket;

    /** Stream for sending objects to the server. */
    private ObjectOutputStream output;

    /** Stream for receiving objects from the server. */
    private ObjectInputStream input;

    /**
     * Constructs a new Client instance with a specified logger.
     *
     * @param logger The logger instance configured for logging to a file.
     */
    public Client(Logger logger) {
        this.logger = logger;
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
    public void initialize(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String username;

            while (true) {
                System.out.print(Constants.ENTER_USERNAME);
                username = reader.readLine();
                output.writeObject(username);
                logger.info(Constants.LOG_AUTH_ATTEMPT + username);

                Object response = input.readObject();
                if (response instanceof String && response.equals("OK")) {
                    logger.info(Constants.LOG_AUTH_SUCCESS + username);
                    break;
                } else {
                    System.out.println(Constants.USERNAME_TAKEN);
                    logger.warning(Constants.LOG_USERNAME_TAKEN);
                }
            }

            while (true) {
                logger.info(Constants.LOG_WAITING_OPPONENT);
                System.out.println(Constants.WAITING_FOR_OPPONENT);
                Object message = input.readObject();
                System.out.println(message);
                logger.info(Constants.LOG_RECEIVED_MESSAGE + message);

                System.out.print(Constants.ENTER_MOVE);
                Move move = Move.valueOf(reader.readLine().toUpperCase());
                output.writeObject(move);
                logger.info(Constants.LOG_MOVE_SENT + move);


                GameResult result = (GameResult) input.readObject();
                System.out.println(Constants.GAME_RESULT + result.getResult());
                logger.info(Constants.LOG_GAME_RESULT + result.getResult());
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, Constants.LOG_CLIENT_ERROR, e);
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    /**
     * Closes the client connection and releases resources.
     * <p>
     * Ensures the socket is properly closed to avoid resource leaks.
     * </p>
     */
    private void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
                logger.info(Constants.LOG_CLIENT_CLOSED);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, Constants.LOG_CLIENT_CLOSE_ERROR, e);
        }
    }
}