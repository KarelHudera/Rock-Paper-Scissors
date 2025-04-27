package karel.hudera.rps.constants;

/**
 * Constants used throughout the Rock-Paper-Scissors game application.
 * <p>
 * This class provides centralized storage for configuration values,
 * messages, and logging strings to ensure consistency across the application.
 * </p>
 */
public final class Constants {
    // Server configuration
    public static final int PORT = 9090;
    public static final String SERVER_ADDRESS = "localhost";
    public static final int MAX_THREADS = 9;

    // Log files
    public static final String LOG_FILE_S = "server.log";
    public static final String LOG_FILE_C = "client.log";

    // Game moves
    public static final String ROCK = "ROCK";
    public static final String PAPER = "PAPER";
    public static final String SCISSORS = "SCISSORS";

    // General responses
    public static final String OK = "OK";
    public static final String AUTH_FAILED = "Invalid username or password, try again.";
    public static final String USERNAME_TAKEN = "This username is already logged in, try a different one.";

    // Game messages
    public static final String GAME_STARTED = "Game started! You are playing against: ";
    public static final String DRAW = "It's a draw!";
    public static final String WINS = " wins!";
    public static final String WAITING_FOR_OPPONENT = "Waiting for opponent...";
    public static final String GAME_RESULT = "Game result: ";
    public static final String ENTER_MOVE = "Enter your move (ROCK, PAPER, SCISSORS): ";
    public static final String ENTER_USERNAME = "Enter your username: ";
    public static final String ENTER_PASSWORD = "Enter your password: ";

    // Logging messages
    public static final String LOG_GAME_SESSION_STARTED = "🎮 Starting game between %s and %s";
    public static final String LOG_GAME_RESULT = "🏆 Game result: %s";
    public static final String LOG_GAME_ERROR = "❌ Error in game session: ";
    public static final String LOG_SERVER_STOPPED = "⚙️ Server stopped.";
    public static final String LOG_SERVER_RUNNING = "⚙️ Server is running";
    public static final String LOG_AUTH_ATTEMPT = "🔑 Attempting to authenticate username: ";
    public static final String LOG_AUTH_SUCCESS = "✅ Username accepted: ";
    public static final String LOG_USERNAME_TAKEN = "❌ Username already taken, user prompted to try again.";
    public static final String LOG_WAITING_OPPONENT = "⏳ Waiting for opponent...";
    public static final String LOG_RECEIVED_MESSAGE = "📩 Received message: ";
    public static final String LOG_MOVE_SENT = "✊✋✌️ Move sent: ";
    public static final String LOG_CLIENT_ERROR = "❌ Error in client operation";
    public static final String LOG_CLIENT_CLOSED = "⚙️ Client connection closed.";
    public static final String LOG_CLIENT_CLOSE_ERROR = "❌ Error closing client connection";
    public static final String LOG_USERS_LOADED = "✅ Allowed users loaded: ";
    public static final String LOG_DUPLICATE_LOGIN = "❌ Duplicate login attempt for: ";
    public static final String LOG_AUTH_FAIL = "❌ Authentication failed for: ";
    public static final String LOG_NO_USERS_CONNECTED = "👤 No users connected.";
    public static final String LOG_USERS_CONNECTED = "👥 %d user(s) connected.";
    public static final String ERROR_SERVER_SOCKET_CLOSE_FAILED = "❌ Error closing server socket: %s";
    public static final String ERROR_LOGIN_FAILED = "❌ Login error: %s";
    public static final String LOG_CLIENT_CONNECTED = "🔗 Client %s:%d has connected.";
    public static final String WELCOME_MESSAGE = "Connected to RPS server";
    public static final String LOG_WELCOME_SENT = "📤 Sent welcome message to client %s:%d";
    public static final String LOG_RECEIVED_FROM_CLIENT = "📩 Received from %s:%d: %s";
    public static final String SERVER_ECHO = "Server echoes: ";
    public static final String LOG_SENT_TO_CLIENT = "📤 Sent message to %s:%d: %s";
    public static final String LOG_CLIENT_DISCONNECTED = "⚙️ Client %s:%d has disconnected.";
    public static final String ERROR_CLIENT_COMMUNICATION = "❌ Error with client %s:%d: %s";
    public static final String ERROR_CLOSING_CONNECTION = "❌ Error closing connection with %s:%d: %s";
    public static final String ERROR_LOGGER = "❌ Failed to configure logger: ";
}