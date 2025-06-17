package karel.hudera.rps.game;

/**
 * Klient posílá serveru zvolený Enum z Move
 * **/
public class GameAction extends GameMessage {
    private static final long serialVersionUID = 5L;

    private String playerId;
    private Move choice;

    public GameAction(String playerId, Move choice) {
        this.playerId = playerId;
        this.choice = choice;
    }

    public String getPlayerId() {
        return playerId;
    }

    public Move getChoice() {
        return choice;
    }

    @Override
    public String toString() {
        return "GameAction{" +
                "playerId='" + playerId + '\'' +
                ", choice=" + choice +
                '}';
    }
}
