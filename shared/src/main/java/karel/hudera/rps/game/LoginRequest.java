package karel.hudera.rps.game;

/**
 * Posílá klient serveru
 * TODO: obsahuje jen username, přidat password
 * **/
public class LoginRequest extends GameMessage{
    private static final long serialVersionUID = 2L; // Unikátní serialVersionUID pro tuto třídu

    private String username;

    public LoginRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                '}';
    }
}
