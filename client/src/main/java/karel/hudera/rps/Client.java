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
            logger.info("Connected to " + socket.getRemoteSocketAddress());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            System.out.println(reader.readLine()); // Welcome message

            // Authentication loop
            while (true) {
                System.out.print("Enter username: ");
                String username = scanner.nextLine();


                System.out.print("Enter password: ");
                String password = scanner.nextLine();

                writer.println(username + ":" + password);

                String response = reader.readLine();
                logger.info("Server response: " + response);

                if (response.startsWith("LOGIN_SUCCESS")) {
                    System.out.println("Login successful");
                    break;
                } else {
                    System.out.println("Login failed. Please try again.");
                }
            }

            // Message sending loop
            while (true) {
                System.out.print("Enter message (or EXIT to quit): ");
                String message = scanner.nextLine();
                writer.println(message);

                if (message.equalsIgnoreCase("EXIT")) {
                    System.out.println("Exiting...");
                    break;
                }
                System.out.println("Server response: " + reader.readLine());
            }

        } catch (IOException e) {
            logger.severe("Error: " + e.getMessage());
        }
    }
}