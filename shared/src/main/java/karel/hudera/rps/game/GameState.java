package karel.hudera.rps.game;

public class GameState extends GameMessage{
    private static final long serialVersionUID = 4L; // Unikátní serialVersionUID

    // Enum pro různé stavy hry, které pomáhají klientovi pochopit, co se děje.
    public enum GameStatus {
        WAITING_FOR_PLAYERS,    // Server čeká na druhého hráče, aby se připojil/přihlásil
        LOBBY_READY,            // Oba hráči jsou přihlášeni a připraveni hrát (před prvním kolem)
        ROUND_STARTED,          // Kolo začalo, hráči mohou provést tah
        PLAYER_MADE_CHOICE,     // Jeden hráč už hrál, server čeká na tah druhého
        ROUND_ENDED,            // Kolo skončilo, jsou k dispozici výsledky kola a aktualizované skóre
        GAME_OVER               // Hra skončila, je k dispozici celkový vítěz
    }

    private GameStatus status;          // Aktuální stav hry
    private String message;             // Obecná textová zpráva pro klienta (např. "Čekám na protihráče", "Vyhrál jsi!")
    private Move yourChoice;    // Tah, který provedl klient, kterému je tato zpráva poslána
    private Move opponentChoice; // Tah protihráče (odhalen až po skončení kola)
    private String roundResult;         // Textový výsledek kola (např. "You win!", "It's a draw!")
    private String winnerId;            // ID hráče, který vyhrál aktuální kolo nebo celou hru
    private int player1Score;           // Skóre prvního hráče
    private int player2Score;           // Skóre druhého hráče
    private String player1Id;           // ID prvního hráče ve hře
    private String player2Id;           // ID druhého hráče ve hře

    // Konstruktor pro inicializaci základního stavu
    public GameState(GameStatus status, String message) {
        this.status = status;
        this.message = message;
        this.yourChoice = Move.NONE;
        this.opponentChoice = Move.NONE;
        this.roundResult = null;
        this.winnerId = null;
        this.player1Score = 0;
        this.player2Score = 0;
        this.player1Id = null;
        this.player2Id = null;
    }

    // Settery pro plynulé nastavení dalších detailů stavu (Fluent API)
    public GameState setYourChoice(Move yourChoice) {
        this.yourChoice = yourChoice;
        return this;
    }

    public GameState setOpponentChoice(Move opponentChoice) {
        this.opponentChoice = opponentChoice;
        return this;
    }

    public GameState setRoundResult(String roundResult) {
        this.roundResult = roundResult;
        return this;
    }

    public GameState setWinnerId(String winnerId) {
        this.winnerId = winnerId;
        return this;
    }

    public GameState setScores(int player1Score, int player2Score) {
        this.player1Score = player1Score;
        this.player2Score = player2Score;
        return this;
    }

    public GameState setPlayerIds(String player1Id, String player2Id) {
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        return this;
    }


    // Gettery pro přístup k datům ze zprávy
    public GameStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Move getYourChoice() {
        return yourChoice;
    }

    public Move getOpponentChoice() {
        return opponentChoice;
    }

    public String getRoundResult() {
        return roundResult;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public String getPlayer1Id() {
        return player1Id;
    }

    public String getPlayer2Id() {
        return player2Id;
    }

    @Override
    public String toString() {
        return "GameState{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", yourChoice=" + yourChoice +
                ", opponentChoice=" + opponentChoice +
                ", roundResult='" + roundResult + '\'' +
                ", winnerId='" + winnerId + '\'' +
                ", player1Score=" + player1Score +
                ", player2Score=" + player2Score +
                ", player1Id='" + player1Id + '\'' +
                ", player2Id='" + player2Id + '\'' +
                '}';
    }
}
