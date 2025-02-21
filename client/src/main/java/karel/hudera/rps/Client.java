package karel.hudera.rps;

import karel.hudera.rps.utils.LoggingClient;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static final Logger logger = Logger.getLogger(Client.class.getName());

    public static void main(String args[]) throws IOException {

        LoggingClient.configureLogger(logger, "client.log");
        try {
            // create a socket to connect to the server running on localhost at port number 9090
            logger.info("Attempting to connect to server...");
            Socket socket = new Socket("localhost", 9090);
            logger.info("Connected to server");

            // Setup output stream to send data to the server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Setup input stream to receive data from the server
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send message to the server
            logger.info("Sending message to server...");
            out.println("Hello from client!");

            // Receive response from the server
            String response = in.readLine();
            System.out.println("Server says: " + response);

            // Close the socket
            socket.close();
            logger.info("Connection closed");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error: ", e.toString());
        }
    }
}