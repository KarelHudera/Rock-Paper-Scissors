package karel.hudera.rps.auth;

import karel.hudera.rps.game.LoginRequest;
import karel.hudera.rps.game.LoginResponse;
import karel.hudera.rps.utils.ServerLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class LoginService {

    private static final Logger logger = ServerLogger.INSTANCE;

    private static final Map<String, String> DEMO_USERS = new HashMap<>();
    static {
        DEMO_USERS.put("adela", "heslo123");
        DEMO_USERS.put("karel", "password");
        DEMO_USERS.put("hrac", "hrac123");
        DEMO_USERS.put("hrac2", "hrac456");
        DEMO_USERS.put("admin", "admin");
        DEMO_USERS.put("test", "test");
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

            if (isValidUser(username, password)) {
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
}