package karel.hudera.rps.server;

import java.io.*;
import java.net.Socket;


/**
 * Handles communication between the server and a connected client.
 * Each client runs in a separate thread.
 */
class ClientHandler implements Runnable {
    private Socket socket;
    ObjectInputStream input;
    private ObjectOutputStream output;
    String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            while (true) {
                username = (String) input.readObject();
                if (Server.addUser(username)) {
                    output.writeObject("OK");
                    break;
                } else {
                    output.writeObject("Username already taken");
                }
            }

            System.out.println(username + " has connected.");
            Server.addWaitingPlayer(this);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            Server.removeUser(username);
        }
    }

    public void sendMessage(Object message) throws IOException {
        output.writeObject(message);
    }
}
