package karel.hudera.rps.game;
/**
 * Odpověď serveru na LoginResponse
 * **/

public class LoginResponse extends GameMessage {
    private static final long serialVersionUID = 3L;

    private boolean success;
    private String message;

    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
