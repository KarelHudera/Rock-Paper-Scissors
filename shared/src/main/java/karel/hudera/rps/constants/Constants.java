package karel.hudera.rps.constants;

public class Constants {
    public static final int PORT = 9090;
    public static final String SERVER_ADDRESS = "localhost";

    // Game moves
    public static final String ROCK = "ROCK";
    public static final String PAPER = "PAPER";
    public static final String SCISSORS = "SCISSORS";

    // General responses
    public static final String OK = "OK";

    // Game messages
    public static final String GAME_RESULT = "🏆 Game result: ";
    public static final String ENTER_USERNAME = "🔑 Enter your username: ";
    public static final String USERNAME_TAKEN = "❌ Username already taken, try again.";
    public static final String WAITING_FOR_OPPONENT = "⏳ Waiting for opponent...";
    public static final String ENTER_MOVE = "✊✋✌️ Enter your move (ROCK, PAPER, SCISSORS): ";

    // Logging messages
    public static final String LOG_AUTH_ATTEMPT = "🔑 Attempting to authenticate username: ";
    public static final String LOG_AUTH_SUCCESS = "✅ Username accepted: ";
    public static final String LOG_USERNAME_TAKEN = "❌ Username already taken, user prompted to try again.";
    public static final String LOG_WAITING_OPPONENT = "⏳ Waiting for opponent...";
    public static final String LOG_RECEIVED_MESSAGE = "📩 Received message: ";
    public static final String LOG_MOVE_SENT = "✊✋✌️ Move sent: ";
    public static final String LOG_GAME_RESULT = "🏆 Game result received: ";
    public static final String LOG_CLIENT_ERROR = "❌ Error in client operation";
    public static final String LOG_CLIENT_CLOSED = "⚙️ Client connection closed.";
    public static final String LOG_CLIENT_CLOSE_ERROR = "❌ Error closing client connection";
}
