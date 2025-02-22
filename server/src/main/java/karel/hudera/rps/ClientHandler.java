package karel.hudera.rps;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import static karel.hudera.rps.Server.logger;

/**
 * Handles communication between the server and a connected client.
 * Each client runs in a separate thread.
 */
public class ClientHandler extends Thread {
    private final Socket socket;
    private final Map<String, String> activeUsers;
    private final Map<String, String> validUsers;
    private PrintWriter writer;
    private BufferedReader reader;
    private String username;
    private String password;


    /**
     * Creates a new {@code ClientHandler} to manage a client's session.
     *
     * @param socket      the client's socket connection
     * @param activeUsers a shared map of active users and their authentication tokens
     */
    public ClientHandler(Socket socket, Map<String, String> activeUsers, Map<String, String> validUsers) {
        this.socket = socket;
        this.activeUsers = activeUsers;
        this.validUsers = validUsers;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println("Welcome! Please enter your username and password:");

            // Read the combined username and password
            String userCredentials = reader.readLine();
            String[] credentials = userCredentials.split(":");


            if (credentials.length == 2) {
                username = credentials[0].trim();
                password = credentials[1].trim();

                logger.info("Received login attempt - Username: [" + username + "], Password: [" + password + "]");

                if (validUsers.containsKey(username) && validUsers.get(username).equals(password)) {
                    String token = UUID.randomUUID().toString();
                    activeUsers.put(username, token);
                    writer.println("LOGIN_SUCCESS|" + token);
                    logger.info("User " + username + " logged in.");

                    // Keep session open for further communication
                    while (true) {
                        String clientMessage = reader.readLine();
                        if (clientMessage == null || clientMessage.equalsIgnoreCase("EXIT")) {
                            break;
                        }
                        // Placeholder for future gameplay commands
                        writer.println("SERVER_ACK: Received -> " + clientMessage);
                    }
                } else {
                    writer.println("LOGIN_FAILED");
                    logger.warning("Login failed for username: [" + username + "]");
                    socket.close();
                }
            } else {
                writer.println("LOGIN_FAILED");
                logger.warning("Login failed for username: [" + username + "]");
                socket.close();
            }

        } catch (IOException e) {
            logger.log(Level.WARNING, "Connection lost with " + username, e);
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        if (username != null) {
            activeUsers.remove(username);
            logger.info("User " + username + " logged out.");
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}