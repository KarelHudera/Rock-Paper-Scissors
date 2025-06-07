package karel.hudera.rps.server;

import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.game.GameAction;
import karel.hudera.rps.game.GameState;
import karel.hudera.rps.game.LoginRequest;
import karel.hudera.rps.game.LoginResponse;
import karel.hudera.rps.utils.ServerLogger;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
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
    //private PrintWriter out;
    //private BufferedReader in;
    //lepší použít toto pro komunikaci se serializable objekty
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String clientAddress;
    private int clientPort;
    private String username;

    /**
     * Constructs a new ClientHandler to manage communication with a connected client.
     *
     * @param clientSocket The socket through which the client communicates with the server.
     */
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.clientAddress = clientSocket.getInetAddress().getHostAddress();
        this.clientPort = clientSocket.getPort();
        logger.info(String.format(Constants.LOG_CLIENT_CONNECTED, clientAddress, clientPort));
    }

    /**
     * Executes the client handling logic in a separate thread.
     * This method initializes streams, processes client messages, and handles connection closure.
     * All activities are logged to both console and file according to the logging configuration.
     */
    @Override
    public void run() {
        try {
            output = new ObjectOutputStream(clientSocket.getOutputStream());
            input = new ObjectInputStream(clientSocket.getInputStream());

            /**
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
        }**/
            GameState welcomeState = new GameState(GameState.GameStatus.WAITING_FOR_PLAYERS, Constants.WELCOME_MESSAGE);
            output.writeObject(welcomeState);
            output.flush(); // Důležité: Vždy po odeslání objektu stream vyprázdněte (flush)!
            logger.info(String.format(Constants.LOG_WELCOME_SENT, clientAddress, clientPort));


            boolean authenticated = handleAuthentication();

            if (authenticated) {
                logger.info("Client " + username + " is now authenticated and ready for game messages.");
                // TODO: Zde bude následovat herní smyčka
                // Po úspěšné autentizaci server čeká na herní akce (GameAction) od klienta
                // a posílá mu zpět aktuální stav hry (GameState).

                // Příklad základní herní smyčky (pro demonstraci komunikace):
                while (true) {
                    // Přečte obecný GameMessage objekt (může to být GameAction atd.)
                    Object receivedObject = input.readObject();
                    logger.info(String.format(Constants.LOG_RECEIVED_FROM_CLIENT, clientAddress, clientPort, receivedObject.getClass().getSimpleName()));

                    if (receivedObject instanceof GameAction) {
                        GameAction action = (GameAction) receivedObject;
                        logger.info("Received GameAction from " + action.getPlayerId() + ": " + action.getChoice());

                        // TODO: ZDE PŘEDEJDETE AKCI HERNÍMU MANAŽEROVI
                        // Např.: gameManager.handlePlayerAction(this, action);
                        // 'this' odkazuje na tento ClientHandler, aby GameManager věděl, od koho akce přišla.

                    } else if (receivedObject instanceof LoginRequest) {
                        // Toto by se nemělo dít po autentizaci, ale je dobré to ošetřit
                        logger.warning("Received LoginRequest after authentication from " + clientAddress + ":" + clientPort);
                        // Můžete odeslat chybovou odpověď nebo ignorovat
                    } else {
                        logger.warning("Received unexpected object type: " + receivedObject.getClass().getName());
                    }
                   }

            } else {
                // Pokud autentizace selže, spojení se zavře ve `finally` bloku.
                logger.warning("Authentication failed for client " + clientAddress + ":" + clientPort + ". Closing connection.");
            }

        } catch (IOException e) {
            // Chyby při čtení/zápisu streamů (např. klient se odpojil, nebo problém se sítí)
            logger.log(Level.WARNING, String.format(Constants.ERROR_CLIENT_COMMUNICATION, clientAddress, clientPort, e.getMessage()), e);
        } catch (ClassNotFoundException e) {
            // Chyba deserializace (server nemůže najít třídu přijatého objektu,
            // nebo verze třídy neodpovídají)
            logger.log(Level.SEVERE, String.format("❌ Deserialization error with client %s:%d: %s", clientAddress, clientPort, e.getMessage()), e);
        } finally {
            closeConnection(clientAddress, clientPort); // Vždy se ujistěte, že se zdroje zavřou
            logger.info(String.format(Constants.LOG_CLIENT_DISCONNECTED, clientAddress, clientPort));
        }

    }

    private boolean handleAuthentication() throws IOException, ClassNotFoundException {
        Object receivedObject = input.readObject(); // Čeká na objekt LoginRequest
        logger.info(String.format(Constants.LOG_RECEIVED_FROM_CLIENT, clientAddress, clientPort, receivedObject.getClass().getSimpleName()));

        if (receivedObject instanceof LoginRequest) {
            LoginRequest request = (LoginRequest) receivedObject;
            username = request.getUsername(); // Získáme uživatelské jméno z požadavku

            logger.info(Constants.LOG_AUTH_ATTEMPT + username);

            // Zde by měla být skutečná logika autentizace (ověření uživatelského jména a hesla)
            // a kontrola, zda uživatel není již přihlášen.
            // Pro jednoduchost: akceptujeme jakékoli neprázdné uživatelské jméno
            if (username != null && !username.trim().isEmpty()) {
                LoginResponse response = new LoginResponse(true, Constants.OK);
                output.writeObject(response);
                output.flush();
                logger.info(Constants.LOG_AUTH_SUCCESS + username);
                return true;
            } else {
                // Pokud uživatelské jméno chybí nebo je prázdné
                LoginResponse response = new LoginResponse(false, Constants.AUTH_FAILED);
                output.writeObject(response);
                output.flush();
                logger.warning(Constants.LOG_AUTH_FAIL + (username != null ? username : "null username"));
                username = null; // Autentizace selhala
                return false;
            }
        } else {
            // Pokud klient nepošle očekávaný LoginRequest
            logger.warning(String.format("Received unexpected object type for authentication from %s:%d: %s", clientAddress, clientPort, receivedObject.getClass().getName()));
            LoginResponse response = new LoginResponse(false, Constants.AUTH_FAILED); // Pošleme chybu
            output.writeObject(response);
            output.flush();
            return false;
        }
    }

    /**
     * Closes all resources associated with this client connection.
     *
     * @param clientAddress The client's IP address
     * @param clientPort    The client's port number
     */
    private void closeConnection(String clientAddress, int clientPort) {
        /**try {
            // Close resources
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();

            logger.info(String.format(Constants.LOG_CLIENT_DISCONNECTED, clientAddress, clientPort));
        } catch (IOException e) {
            logger.severe(String.format(Constants.ERROR_CLOSING_CONNECTION, clientAddress, clientPort, e.getMessage()));
        }**/
        try {
            if (output != null) output.close();
            if (input != null) input.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, String.format(Constants.ERROR_CLOSING_CONNECTION, clientAddress, clientPort, e.getMessage()), e);
        }
    }

    /**
     * Sends a message to the client.
     *
     * @param message The message to send to the client.
     * @return true if the message was sent successfully, false otherwise
     */
    /**
     No more in use due to object serialization usage
    public boolean sendMessage(String message) {
        if (out != null && !clientSocket.isClosed()) {
            out.println(message);
            logger.info(String.format(Constants.LOG_SENT_TO_CLIENT,
                    clientSocket.getInetAddress(), clientSocket.getPort(), message));
            return true;
        }
        return false;
    }

     **/

    public String getUsername() {
        return username;
    }

}