package karel.hudera.rps;

import karel.hudera.rps.utils.Logging;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Represents the client that connects to the Rock-Paper-Scissors server.
 */
public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9090;
    private static final Logger logger = Logger.getLogger(Client.class.getName());

    private PrintWriter writer;
    private BufferedReader reader;
    private String token;

    /**
     * Main method to start the client.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        // Configure logging for the client
        Logging.configureLogger(logger, "client.log");

        // Start the client
        new Client().start();
    }

    /**
     * Starts the client and handles user interaction.
     */
    public void start() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            System.out.println(reader.readLine()); // Welcome message

            // Authentication
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            writer.println("LOGIN|" + username + "|" + password);
            String response = reader.readLine();
            if (response.startsWith("LOGIN_SUCCESS")) {
                token = response.split("\\|")[1];
                System.out.println("Login successful! Token: " + token);
            } else {
                System.out.println("Login failed.");
                return;
            }

            // Sending a move
            System.out.print("Enter your move (ROCK, PAPER, SCISSORS): ");
            String move = scanner.nextLine();
            writer.println("PLAY|" + token + "|" + move);
            System.out.println("Server response: " + reader.readLine());

        } catch (IOException e) {
            logger.severe("Error: " + e.getMessage());
        }
    }
}