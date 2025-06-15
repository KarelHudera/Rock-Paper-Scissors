package karel.hudera.rps.game;

public class GameMove extends GameMessage{
    private static final long serialVersionUID = 1L;

    private Move move;

    public GameMove(Move move) {
        this.move = move;
    }

    public Move getMove() {
        return move;
    }

    @Override
    public String toString() {
        return "GameMove{move=" + move + "}";
    }
}
