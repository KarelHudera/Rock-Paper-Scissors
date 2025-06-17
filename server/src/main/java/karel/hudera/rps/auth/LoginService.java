package karel.hudera.rps.auth;

import karel.hudera.rps.game.LoginRequest;
import karel.hudera.rps.game.LoginResponse;
import karel.hudera.rps.utils.ServerLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.logging.Logger;

public class LoginService {

    private static final Logger logger = ServerLogger.INSTANCE;

    private static final Map<String, String> DEMO_USERS = new HashMap<>();
    private static final Set<String> loggedInUsers = Collections.synchronizedSet(new HashSet<>());

    static {
        try (InputStream input = LoginService.class.getClassLoader().getResourceAsStream("users.properties")) {
            if (input == null) {
                logger.warning("Unable to find users.properties");
            } else {
                Properties props = new Properties();
                props.load(input);
                for (String key : props.stringPropertyNames()) {
                    DEMO_USERS.put(key.toLowerCase(), props.getProperty(key));
                }
                logger.info("Loaded users from users.properties");
            }
        } catch (IOException e) {
            logger.warning("Error loading users.properties: " + e.getMessage());
        }
    }

    public String authenticate(ObjectInputStream in, ObjectOutputStream out, String clientInfo) {
        try {
            Object obj = in.readObject();
            if (!(obj instanceof LoginRequest loginRequest)) {
                logger.warning("Unexpected first message from client: " + obj.getClass().getName());
                out.writeObject(new LoginResponse(false, "LoginRequest should be first message"));
                return null;
            }

            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();

            logger.info("Received LOGIN request from " + username + " at " + clientInfo);

            if (isUserAlreadyLoggedIn(username)) {
                out.writeObject(new LoginResponse(false, "User is already logged in elsewhere"));
                logger.warning("Duplicate login attempt for user: " + username + " from " + clientInfo);
                return null;
            }

            if (isValidUser(username, password)) {
                loggedInUsers.add(username.toLowerCase());
                out.writeObject(new LoginResponse(true, "Logged in successfully!"));
                logger.info("Authentication successful for user: " + username + " from " + clientInfo);
                return username;
            } else {
                out.writeObject(new LoginResponse(false, "Wrong username or password"));
                logger.warning("Authentication failed for user: " + username + " from " + clientInfo);
                return null;
            }

        } catch (IOException | ClassNotFoundException e) {
            logger.warning("Error during authentication for " + clientInfo + ": " + e.getMessage());
            return null;
        }
    }

    private boolean isValidUser(String username, String password) {
        if (username == null || password == null) return false;
        String expectedPassword = DEMO_USERS.get(username.toLowerCase());
        return expectedPassword != null && expectedPassword.equals(password);
    }

    private boolean isUserAlreadyLoggedIn(String username) {
        return loggedInUsers.contains(username.toLowerCase());
    }
}