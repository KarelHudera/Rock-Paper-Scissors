package karel.hudera.rps.server;

import karel.hudera.rps.game.GameResult;
import karel.hudera.rps.game.Move;

import java.io.IOException;

class GameSession {
    private ClientHandler player1;
    private ClientHandler player2;

    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public void start() {
        System.out.println("Starting game between " + player1.username + " and " + player2.username);
        new Thread(this::playGame).start();
    }

    private void playGame() {
        try {
            player1.sendMessage("Game started! You are playing against " + player2.username);
            player2.sendMessage("Game started! You are playing against " + player1.username);

            Move move1 = (Move) player1.input.readObject();
            Move move2 = (Move) player2.input.readObject();

            String result = determineWinner(move1, move2);
            GameResult gameResult = new GameResult(player1.username, move1.name(), player2.username, move2.name(), result);

            player1.sendMessage(gameResult);
            player2.sendMessage(gameResult);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String determineWinner(Move move1, Move move2) {
        if (move1 == move2) return "It's a draw!";
        if ((move1 == Move.ROCK && move2 == Move.SCISSORS) ||
                (move1 == Move.SCISSORS && move2 == Move.PAPER) ||
                (move1 == Move.PAPER && move2 == Move.ROCK)) {
            return player1.username + " wins!";
        }
        return player2.username + " wins!";
    }
}
