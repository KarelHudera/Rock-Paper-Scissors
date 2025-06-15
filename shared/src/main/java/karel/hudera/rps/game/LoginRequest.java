package karel.hudera.rps.game;

/**
 * Posílá klient serveru
 * **/
public class LoginRequest extends GameMessage{
    private static final long serialVersionUID = 2L; // Unikátní serialVersionUID pro tuto třídu

    private String username;
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                '}';
    }
}
