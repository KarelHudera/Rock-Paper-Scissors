package karel.hudera.rps.game;

public class RoundResult extends GameMessage {
    private static final long serialVersionUID = 1L;

    private Move yourMove;        // Tvůj tah (Enum Move)
    private Move opponentMove;    // Tah protihráče (Enum Move)
    private Result roundResult;   // Výsledek kola (Enum Result)
    private int player1Score;     // Skóre prvního hráče (tvého) po tomto kole
    private int player2Score;     // Skóre druhého hráče (soupeře) po tomto kole


    // Konstruktor pro RoundResult
    public RoundResult(Move yourMove, Move opponentMove, Result roundResult, int player1Score, int player2Score) {
        this.yourMove = yourMove;
        this.opponentMove = opponentMove;
        this.roundResult = roundResult;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
    }

    // Gettery
    public Move getYourMove() {
        return yourMove;
    }

    public Move getOpponentMove() {
        return opponentMove;
    }

    public Result getRoundResult() {
        return roundResult;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    @Override
    public String toString() {
        return "RoundResult{" +
                "yourMove=" + yourMove +
                ", opponentMove=" + opponentMove +
                ", roundResult=" + roundResult +
                ", player1Score=" + player1Score +
                ", player2Score=" + player2Score +
                '}';
    }
}
