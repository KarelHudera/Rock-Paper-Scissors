package karel.hudera.rps.utils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;

public class LoggingClient {
    public static void configureLogger(Logger logger, String logFileName) throws IOException {

        // Create the "target/logs" directory if it doesn't exist
        Path logsDir = Paths.get("logs");
        if (!Files.exists(logsDir)) {
            Files.createDirectories(logsDir);
        }

        // Disable default logging
        logger.setUseParentHandlers(false);

        // Console handler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());

        // File handler (append mode)
        String logFilePath = logsDir.resolve(logFileName).toString();
        FileHandler fileHandler = new FileHandler(logFilePath, 1024 * 1024, 5, true);  // 1MB files, keep 5
        fileHandler.setFormatter(new SimpleFormatter());

        // Add handlers
        logger.addHandler(consoleHandler);
        logger.addHandler(fileHandler);

        // Set levels
        logger.setLevel(Level.ALL);
        consoleHandler.setLevel(Level.INFO);
        fileHandler.setLevel(Level.ALL);
    }
}