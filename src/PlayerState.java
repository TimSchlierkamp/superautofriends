
import java.util.ArrayList;
import java.util.List
;public class PlayerState {
    private String playerName;
    private boolean ready;
    private boolean buyPhaseDone;
    private List<FriendData> team; // Neuer Feld

    public PlayerState(String playerName) {
        this.playerName = playerName;
        this.ready = true;
        this.buyPhaseDone = true;
        this.team = new ArrayList<>(); // Initialisierung
    }

    // Getter und Setter
    public String getPlayerName() {
        return playerName;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
        System.out.println("Player '" + playerName + "' set to ready: " + ready);
    }

    public boolean isBuyPhaseDone() {
        return buyPhaseDone;
    }

    public void setBuyPhaseDone(boolean buyPhaseDone) {
        this.buyPhaseDone = buyPhaseDone;
        System.out.println("Player '" + playerName + "' buyPhaseDone: " + buyPhaseDone);
    }

    public List<FriendData> getTeam() {
        return team;
    }

    public void setTeam(List<FriendData> team) {
        this.team = team;
    }
}
