package karel.hudera.rps.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;

/**
 * Utility class for configuring a logger with both console and file output.
 */
public class Logging {

    /**
     * Configures the given logger to log messages to both the console and a file.
     * <p>
     * Logs are stored in the "logs" directory with a rotating file handler.
     * </p>
     *
     * @param logger      the {@link Logger} instance to configure
     * @param logFileName the name of the log file (without path)
     */
    public static void configureLogger(Logger logger, String logFileName) {
        try {
            // Create the "logs" directory if it doesn't exist
            Path logsDir = Paths.get("logs");
            if (Files.notExists(logsDir)) {
                Files.createDirectories(logsDir);
            }

            // Disable default parent handlers
            logger.setUseParentHandlers(false);

            // Console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());

            // File handler (append mode, rotating logs)
            String logFilePath = logsDir.resolve(logFileName).toString();
            FileHandler fileHandler = new FileHandler(logFilePath, 1024 * 1024, 5, true); // 1MB per file, max 5 files
            fileHandler.setFormatter(new SimpleFormatter());

            // Add handlers to logger
            logger.addHandler(consoleHandler);
            logger.addHandler(fileHandler);

            // Set log levels
            logger.setLevel(Level.ALL);
            consoleHandler.setLevel(Level.INFO);
            fileHandler.setLevel(Level.ALL);

        } catch (IOException e) {
            System.err.println("Failed to configure logger: " + e.getMessage());
        }
    }
}