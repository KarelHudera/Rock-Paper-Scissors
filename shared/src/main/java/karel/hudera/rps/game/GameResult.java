package karel.hudera.rps.game;

import java.io.Serializable;
/**
 * To be Deleted
 * **/

public class GameResult extends GameMessage {
    private static final long serialVersionUID = 9L;
    private String player1;
    private String move1;
    private String player2;
    private String move2;
    private String result;

    public GameResult(String player1, String move1, String player2, String move2, String result) {
        this.player1 = player1;
        this.move1 = move1;
        this.player2 = player2;
        this.move2 = move2;
        this.result = result;
    }

    public String getResult() {
        return result;
    }
}
