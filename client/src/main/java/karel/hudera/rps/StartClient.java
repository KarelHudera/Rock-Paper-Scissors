package karel.hudera.rps;

import java.util.logging.Logger;

import karel.hudera.rps.client.Client;
import karel.hudera.rps.utils.Logging;

/**
 * Entry point for starting the Rock-Paper-Scissors client.
 * <p>
 * This class initializes and connects the client to the server running on <b>localhost:9090</b>.
 * It also configures logging for better debugging and monitoring.
 * </p>
 *
 * @author Karel Hudera
 */
public class StartClient {

    static final Logger logger = Logger.getLogger("ClientLogger");

    public static void main(String[] args) {

        Logging.configureLogger(logger, "client.log");

        Client client = new Client(logger);
        client.initialize("localhost", 9090);
    }
}