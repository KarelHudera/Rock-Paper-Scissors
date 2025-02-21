package karel.hudera.rps;

import karel.hudera.rps.utils.Logging;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public static void main(String args[]) throws IOException {
        Logging.configureLogger(logger, "server.log");

        try {
            // create a server socket on port number 9090
            ServerSocket serverSocket = new ServerSocket(9090);
            logger.info("Server is running and waiting for client connection...");

            // Accept incoming client connection
            Socket clientSocket = serverSocket.accept();
            logger.info("Client connected!");

            // Setup input and output streams for communication with the client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Read message from client
            String message = in.readLine();
            System.out.println("Client says: " + message);

            // Send response to the client
            out.println("Message received by the server.");

            // Close the client socket
            clientSocket.close();
            // Close the server socket
            serverSocket.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error: ", e.toString());
        }
    }
}