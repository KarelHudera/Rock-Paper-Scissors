package karel.hudera.rps.utils;

import karel.hudera.rps.constants.Constants;

import java.util.logging.Logger;

/**
 * Singleton logger for the server application.
 * Configures and provides a shared logger instance for all server components.
 */
public class ServerLogger {

    public static final Logger INSTANCE;

    static {
        INSTANCE = Logger.getLogger("ServerLogger");
        Logging.configureLogger(INSTANCE, Constants.LOG_FILE_S);
    }

    private ServerLogger() {
        // Private constructor to prevent instantiation
    }
}
