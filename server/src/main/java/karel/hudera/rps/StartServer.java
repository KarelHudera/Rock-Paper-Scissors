package karel.hudera.rps;

import java.util.logging.Logger;

import karel.hudera.rps.server.Server;
import karel.hudera.rps.utils.Logging;

/**
 * Entry point for starting the Rock-Paper-Scissors server.
 * <p>
 * This class initializes and starts the server on <b>port 9090</b>.
 * It also configures logging for better debugging and monitoring.
 * </p>
 *
 * @author Karel Hudera
 */
public class StartServer {

    static final Logger logger = Logger.getLogger("ServerLogger");

    public static void main(String[] args) {

        Logging.configureLogger(logger, "server.log");

        Server server = new Server(logger);
        server.initialize(9090);
    }
}