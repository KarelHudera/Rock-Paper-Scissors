package karel.hudera.rps.constants;

public class Constants {
    // Server configuration
    public static final int PORT = 9090;
    public static final String SERVER_ADDRESS = "localhost";
    public static final int MAX_THREADS = 5;

    // Game moves
    public static final String ROCK = "ROCK";
    public static final String PAPER = "PAPER";
    public static final String SCISSORS = "SCISSORS";

    // General responses
    public static final String OK = "OK";
    public static final String AUTH_FAILED = "Invalid username or password, try again.";
    public static final String USERNAME_TAKEN = "This username is already logged in, try a different one.";

    // Game messages
    public static final String GAME_STARTED = "Game started! You are playing against ";
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
    public static final String LOG_CLIENT_CONNECTED = "🔗 Client %s has connected.";
    public static final String LOG_USERS_LOADED = "✅ Allowed users loaded: ";
    public static final String LOG_DUPLICATE_LOGIN = "❌ Duplicate login attempt for: ";
    public static final String LOG_AUTH_FAIL = "❌ Authentication failed for: ";
}