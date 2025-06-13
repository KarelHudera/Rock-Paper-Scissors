package karel.hudera.rps.game;

public class OpponentDisconnected extends GameMessage {
    private static final long serialVersionUID = 1L;

    private String disconnectedPlayerName;

    public OpponentDisconnected(String disconnectedPlayerName) {
        this.disconnectedPlayerName = disconnectedPlayerName;
    }

    public String getDisconnectedPlayerName() {
        return disconnectedPlayerName;
    }

    @Override
    public String toString() {
        return "OpponentDisconnected{" +
                "disconnectedPlayerName='" + disconnectedPlayerName + '\'' +
                '}';
    }
}