package karel.hudera.rps.game;
/**
 * Odpověď serveru na LoginResponse
 * **/

public class LoginResponse extends GameMessage {
    private static final long serialVersionUID = 3L; // Unikátní serialVersionUID

    private boolean success; // true, pokud bylo přihlášení úspěšné
    private String message;  // Důvod neúspěchu nebo uvítací/potvrzující zpráva

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
