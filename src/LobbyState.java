import java.util.List;

/**
 * Repräsentiert den Zustand der Lobby, einschließlich der Spieler, der aktuellen Runde und der Spielphase.
 */
public class LobbyState {
    List<PlayerState> players;
     String turn;
     String gamePhase; // z.B. WAITING, BUY, FIGHT

    /**
     * Konstruktor für LobbyStateJ.
     *
     * @param players    Liste der Spieler im Lobby.
     * @param turn       Der aktuelle Zug oder die aktuelle Runde.
     * @param gamePhase  Die aktuelle Spielphase.
     */
    public LobbyState(List<PlayerState> players, String turn, String gamePhase) {
        this.players = players;
        this.turn = turn;
        this.gamePhase = gamePhase;
    }

    // Getter und Setter

    /**
     * Gibt die Liste der Spieler im Lobby-State zurück.
     *
     * @return Liste der PlayerState-Objekte.
     */
    public List<PlayerState> getPlayers() {
        return players;
    }

    /**
     * Setzt die Liste der Spieler im Lobby-State.
     *
     * @param players Liste der PlayerState-Objekte.
     */
    public void setPlayers(List<PlayerState> players) {
        this.players = players;
    }

    /**
     * Gibt die aktuelle Runde oder den aktuellen Zug zurück.
     *
     * @return String, der die aktuelle Runde oder den Zug beschreibt.
     */
    public String getTurn() {
        return turn;
    }

    /**
     * Setzt die aktuelle Runde oder den aktuellen Zug.
     *
     * @param turn String, der die neue Runde oder den neuen Zug beschreibt.
     */
    public void setTurn(String turn) {
        this.turn = turn;
    }

    /**
     * Gibt die aktuelle Spielphase zurück.
     *
     * @return String, der die aktuelle Spielphase beschreibt.
     */
    public String getGamePhase() {
        return gamePhase;
    }

    /**
     * Setzt die aktuelle Spielphase.
     *
     * @param gamePhase String, der die neue Spielphase beschreibt.
     */
    public void setGamePhase(String gamePhase) {
        this.gamePhase = gamePhase;
    }
}
