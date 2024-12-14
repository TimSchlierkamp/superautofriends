import java.util.List;

class LobbyState {
    List<PlayerState> players;
    String turn;
    String gamePhase; // z.B. WAITING, BUY, WAIT_BUY_DONE, FIGHT

    public LobbyState(List<PlayerState> players, String turn, String gamePhase) {
        this.players = players;
        this.turn = turn;
        this.gamePhase = gamePhase;
    }
}
