package karel.hudera.rps.game;

import java.io.Serializable;

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

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getMove1() {
        return move1;
    }

    public void setMove1(String move1) {
        this.move1 = move1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getMove2() {
        return move2;
    }

    public void setMove2(String move2) {
        this.move2 = move2;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
