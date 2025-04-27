/*
package karel.hudera.rps.server;

import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.game.GameSession;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Authenticatror {

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(Constants.MAX_THREADS);
    private static final BlockingQueue<ClientHandler> waitingPlayers = new LinkedBlockingQueue<>();
    private static final Set<String> activeUsers = Collections.synchronizedSet(new HashSet<>());

    */
/**
     * Adds a player to the waiting queue or starts a new game session if an opponent is available.
     *
     * @param player The {@link ClientHandler} representing the player to add to the waiting queue.
     *//*

    public static void addWaitingPlayer(ClientHandler player) {
        try {
            if (!waitingPlayers.isEmpty()) {
                ClientHandler opponent = waitingPlayers.take();
                new GameSession(player, opponent, logger).start();
            } else {
                waitingPlayers.put(player);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    */
/**
     * Attempts to add a new user to the active users set.
     *
     * @param username The username to add.
     * @return {@code true} if the user was successfully added, {@code false} if the username is already taken.
     *//*

    public static boolean addUser(String username) {
        boolean success = activeUsers.add(username);
        if (success) {
            logger.info(Constants.LOG_AUTH_SUCCESS + username);
        } else {
            logger.warning(Constants.LOG_USERNAME_TAKEN + username);
        }
        return success;
    }

    */
/**
     * Removes a user from the active users set.
     *
     * @param username The username to remove.
     *//*

    public static void removeUser(String username) {
        activeUsers.remove(username);
        logger.info(Constants.LOG_CLIENT_CLOSED + username);
    }

    public static boolean isUserLoggedIn(String username) {
        return activeUsers.contains(username);
    }


    */
/**
     * Validates the username and password against the allowed users.
     *
     * @param username The username to check.
     * @param password The password to verify.
     * @return {@code true} if the credentials are valid, otherwise {@code false}.
     *//*

    public static boolean isValidUser(String username, String password) {
        logger.info(username + " " + password);
        return allowedUsers.containsKey(username) && allowedUsers.get(username).equals(password);
    }

    */
/**
     * Loads valid users. In production, this should be from a database or configuration file.
     *//*

    private void loadUsers() {
        allowedUsers.put("karel", "pass");
        allowedUsers.put("admin", "asd");
        allowedUsers.put("player1", "asd");

        logger.info(Constants.LOG_USERS_LOADED + allowedUsers.keySet());
    }
}
*/
