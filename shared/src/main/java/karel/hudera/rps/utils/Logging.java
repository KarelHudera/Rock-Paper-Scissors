package karel.hudera.rps.utils;

import karel.hudera.rps.constants.Constants;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;

/**
 * Utility class for configuring loggers with both console and file output.
 * <p>
 * This class provides methods to set up logging with proper formatting,
 * file rotation, and directory structure. It ensures consistent logging
 * throughout the Rock-Paper-Scissors application.
 * </p>
 */
public class Logging {

    /**
     * Enhanced formatter for log entries that includes timestamp, level, logger name,
     * thread name, and message with exception details when available.
     */
    private static final Formatter customFormatter = new SimpleFormatter() {
        @Override
        public String format(LogRecord record) {
            String throwableInfo = "";
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                throwableInfo = "\n" + sw;
            }

            return String.format("[%1$tF %1$tT] [%2$s] [%3$s] [Thread: %4$s] %5$s%6$s%n",
                    new java.util.Date(record.getMillis()),
                    record.getLevel().getName(),
                    record.getLoggerName(),
                    Thread.currentThread().getName(),
                    record.getMessage(),
                    throwableInfo
            );
        }
    };

    /**
     * Configures the given logger to log messages to both the console and a file.
     * <p>
     * This method:
     * <ul>
     *   <li>Creates a "logs" directory if it doesn't exist</li>
     *   <li>Sets up console output with INFO level</li>
     *   <li>Sets up file output with ALL level and rotation</li>
     *   <li>Applies consistent formatting to log entries</li>
     * </ul>
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
            consoleHandler.setFormatter(customFormatter);

            // File handler (append mode, rotating logs)
            String logFilePath = logsDir.resolve(logFileName).toString();
            FileHandler fileHandler = new FileHandler(logFilePath, 1024 * 1024, 5, true); // 1MB per file, max 5 files
            fileHandler.setFormatter(customFormatter);

            // Add handlers to logger
            logger.addHandler(consoleHandler);
            logger.addHandler(fileHandler);

            // Set log levels
            logger.setLevel(Level.ALL);
            consoleHandler.setLevel(Level.INFO);
            fileHandler.setLevel(Level.ALL);

        } catch (IOException e) {
            System.err.println(Constants.ERROR_LOGGER + e.getMessage());
        }
    }
}