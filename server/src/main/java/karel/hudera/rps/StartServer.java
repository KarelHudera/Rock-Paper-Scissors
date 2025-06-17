package karel.hudera.rps;

import karel.hudera.rps.constants.Constants;
import karel.hudera.rps.server.Server;

/**
 * Entry point for starting the Rock-Paper-Scissors server.
 * <p>
 * This class initializes and starts the server on <b>port 9090</b>.
 * </p>
 *
 * @author Karel Hudera
 */
public class StartServer {

    /**
     * The main method that starts the Rock-Paper-Scissors server.
     * Creates a new server instance and initializes it.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        Server server = new Server(Constants.PORT);
        server.initialize();
    }
}