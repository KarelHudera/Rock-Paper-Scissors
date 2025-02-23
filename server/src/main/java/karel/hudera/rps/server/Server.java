package karel.hudera.rps.server;

import karel.hudera.rps.constants.Constants;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * The {@code Server} class represents the game server for Rock-Paper-Scissors.
 * It manages client connections, authentication, and multithreaded request handling.
 * <p>
 * Uses an {@link ExecutorService} to limit the number of concurrent client connections.
 * </p>
 */
public class Server {
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();
    private static final BlockingQueue<ClientHandler> waitingPlayers = new LinkedBlockingQueue<>();
    private static final Set<String> activeUsers = Collections.synchronizedSet(new HashSet<>());

    public void initialize(int port) {
        System.out.println(Constants.SERVER_RUNNING);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    public static void addWaitingPlayer(ClientHandler player) {
        try {
            if (!waitingPlayers.isEmpty()) {
                ClientHandler opponent = waitingPlayers.take();
                new GameSession(player, opponent).start();
            } else {
                waitingPlayers.put(player);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static boolean addUser(String username) {
        return activeUsers.add(username);
    }

    public static void removeUser(String username) {
        activeUsers.remove(username);
    }
}