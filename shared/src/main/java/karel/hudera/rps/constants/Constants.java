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
    public static final String WAITING_FOR_GAME_START = "Waiting for game start.";
    public static final String MAKE_YOUR_MOVE = "Make your move.";
    public static final String WAITING_FOR_OPPONENT_MOVE = "Waiting for opponents move.";
    public static final String LOG_GAME_SESSION_CREATED = "Log session created.";
    public static final String WINS_GAME = "Wins game";
    public static final String GAME_DRAW = "Game draw.";
    public static final String WINS_ROUND = "Wins round.";
    public static final String GAME_INTERRUPTED = "Game interrupted.";




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
    public static final String LOG_FAILED_SEND = "❌ Error sending message to client %s:%d";
    public static final String LOG_CLIENT_DISCONNECTED = "⚙️ Client %s:%d has disconnected.";
    public static final String ERROR_CLIENT_COMMUNICATION = "❌ Error with client %s:%d: %s";
    public static final String ERROR_CLOSING_CONNECTION = "❌ Error closing connection with %s:%d: %s";
    public static final String ERROR_LOGGER = "❌ Failed to configure logger: ";


    // New constants for game logic

    // Game moves
    public static final String MOVE_ROCK = "ROCK";
    public static final String MOVE_PAPER = "PAPER";
    public static final String MOVE_SCISSORS = "SCISSORS";

    // Player responses
    public static final String RESP_YES = "YES";
    public static final String RESP_NO = "NO";

    // Game messages to clients
    public static final String MSG_WAITING_FOR_OPPONENT = "Waiting for an opponent...";
    public static final String MSG_OPPONENT_FOUND = "Opponent found! Game is starting.";
    public static final String MSG_REQUEST_MOVE = "Enter your move (ROCK, PAPER, or SCISSORS):";
    public static final String MSG_INVALID_MOVE = "Invalid move! Please enter ROCK, PAPER, or SCISSORS.";
    public static final String MSG_OPPONENT_INVALID = "Your opponent made an invalid move.";
    public static final String MSG_OPPONENT_DISCONNECTED = "Your opponent has disconnected.";
    public static final String MSG_OPPONENT_MOVE = "Your opponent chose: %s";
    public static final String MSG_GAME_WIN = "You win!";
    public static final String MSG_GAME_LOSS = "You lose!";
    public static final String MSG_GAME_TIE = "It's a tie!";
    public static final String MSG_PLAY_AGAIN = "Do you want to play again? (YES/NO)";

    // Game log messages
    public static final String LOG_GAME_MANAGER_STARTED = "Game Manager started successfully";
    public static final String LOG_GAME_MANAGER_SHUTDOWN = "Game Manager is shutting down";
    public static final String LOG_PLAYER_WAITING = "Player %s added to waiting queue";
    public static final String LOG_PLAYER_LEFT_QUEUE = "Player %s removed from waiting queue";
    public static final String LOG_GAME_STARTED = "Game session started between %s and %s";
    public static final String LOG_GAME_TIE = "Game between %s and %s ended in a tie with move: %s";
    public static final String LOG_GAME_WINNER = "Player %s won against %s with move %s vs %s";

    // Game error messages
    public static final String ERROR_MATCHMAKING_INTERRUPTED = "Matchmaking thread was interrupted: %s";
    public static final String ERROR_MATCHMAKING_FAILURE = "Error in matchmaking process: %s";
    public static final String ERROR_GAME_SESSION_FAILURE = "Error in game session between %s and %s: %s";
    public static final String ERROR_GAME_COMMUNICATION = "Error communicating in game between %s and %s: %s";
    public static final String ERROR_PLAY_AGAIN_HANDLING = "Error handling play again requests: %s";
    public static final String ERROR_CLEANUP_INTERRUPTED = "Session cleanup thread was interrupted: %s";
    public static final String ERROR_CLEANUP_FAILURE = "Error in session cleanup process: %s";
}