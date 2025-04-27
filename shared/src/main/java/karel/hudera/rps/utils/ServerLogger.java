package karel.hudera.rps.utils;

import karel.hudera.rps.constants.Constants;

import java.util.logging.Logger;

/**
 * Singleton logger for the server application.
 * Configures and provides a shared logger instance for all server components.
 */
public class ServerLogger {

    // Singleton instance of the logger
    public static final Logger INSTANCE;

    // Static block to initialize the logger
    static {
        // Get the logger and configure it
        INSTANCE = Logger.getLogger("ServerLogger");
        Logging.configureLogger(INSTANCE, Constants.LOG_FILE_S);
    }

    // Private constructor to prevent instantiation
    private ServerLogger() {
        // Empty because I don't want instances to be created
    }
}
