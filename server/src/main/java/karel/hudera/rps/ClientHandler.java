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
    private PrintWriter writer;
    private BufferedReader reader;
    private String username;

    /**
     * Creates a new {@code ClientHandler} to manage a client's session.
     *
     * @param socket       the client's socket connection
     * @param activeUsers  a shared map of active users and their authentication tokens
     */
    public ClientHandler(Socket socket, Map<String, String> activeUsers) {
        this.socket = socket;
        this.activeUsers = activeUsers;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println("Welcome! Use LOGIN|username|password to authenticate.");

            String input;
            while ((input = reader.readLine()) != null) {
                String[] parts = input.split("\\|");
                String command = parts[0];

                if ("LOGIN".equals(command) && parts.length == 3) {
                    handleLogin(parts[1], parts[2]);
                } else if ("PLAY".equals(command) && parts.length == 2) {
                    handlePlay(parts[1]);
                } else {
                    writer.println("ERROR|Unknown command.");
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Connection lost with " + username, e);
        } finally {
            cleanup();
        }
    }

    private void handleLogin(String username, String password) {
        if ("secret".equals(password)) { // Replace with real authentication logic
            this.username = username;
            String token = UUID.randomUUID().toString();
            activeUsers.put(username, token);
            writer.println("LOGIN_SUCCESS|" + token);
            logger.info("User " + username + " logged in.");
        } else {
            writer.println("LOGIN_FAILED");
        }
    }

    private void handlePlay(String token) {
        if (activeUsers.containsValue(token)) {
            writer.println("VALID_MOVE");
        } else {
            writer.println("INVALID_TOKEN");
        }
    }

    private void cleanup() {
        if (username != null) {
            activeUsers.remove(username);
            logger.info("User " + username + " logged out.");
        }
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
}