package karel.hudera.rps.game;

public class TerminateMessage  extends GameMessage{
    private static final long serialVersionUID = 1L;

    // Podle protokolu "TERMINATE Client notifies server it is disconnecting voluntarily."
    // Tato zpráva nepotřebuje žádná další data.

    @Override
    public String toString() {
        return "TerminateMessage{}";
    }
}
