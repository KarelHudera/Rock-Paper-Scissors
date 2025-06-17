package karel.hudera.rps.game;

public class GameStart extends GameMessage{
    private static final long serialVersionUID = 7L; // Vždy dobré přidat

    private String opponentUsername;

    public GameStart(String opponentUsername) {
        this.opponentUsername = opponentUsername;
    }

    public String getOpponentUsername() {
        return opponentUsername;
    }

    @Override
    public String toString() {
        return "GameStart{opponentUsername='" + opponentUsername + "'}";
    }

}
