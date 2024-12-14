public class PlayerState {
    private String playerName;
    private boolean ready;
    private boolean buyPhaseDone;

    public PlayerState(String playerName) {
        this.playerName = playerName;
        this.ready = false;
        this.buyPhaseDone = false;
    }

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
}
