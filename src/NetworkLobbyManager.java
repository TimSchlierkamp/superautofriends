import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * NetworkLobbyManager nutzt Sockets, um den LobbyState zwischen zwei Spielern auszutauschen.
 */
public class NetworkLobbyManager {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    private LobbyState state;

    @SuppressWarnings("unused")
    private boolean isHost;

    public void lobbyHost(int port, String playerName) throws IOException {
        isHost = true;
        // Initialer State
        List<PlayerState> players = new ArrayList<>();
        PlayerState p = new PlayerState(playerName);
        p.setReady(false);
        p.setBuyPhaseDone(false);
        this.state = new LobbyState(players, playerName, "WAITING");
        this.state.players.add(p);

        ServerSocket server = new ServerSocket(port);
        System.out.println("Warte auf Verbindung des anderen Spielers auf Port " + port + "...");
        this.socket = server.accept();
        System.out.println("Spieler verbunden!");

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        // Sende initialen State an Client
        sendState();
        // Warte auf Rückmeldung des Clients (der sich einträgt)
        waitForUpdate();
    }

    public void lobbyJoin(String hostAddress, int port, String playerName) throws IOException {
        isHost = false;
        this.socket = new Socket(hostAddress, port);
        System.out.println("Mit Host verbunden!");

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        // Warte auf State vom Host
        String stateStr = in.readLine();
        this.state = parseState(stateStr);

        // Füge unseren Spieler hinzu und sende aktualisierten State zurück
        PlayerState p = new PlayerState(playerName);
        p.setReady(false);
        p.setBuyPhaseDone(false);
        state.players.add(p);
        sendState();
    }

    public void setPlayerReady(String playerName, boolean ready) throws IOException {
        for (PlayerState p : state.players) {
            if (p.getPlayerName().equals(playerName)) {
                p.setReady(ready);
            }
        }
        if (state.gamePhase.equals("WAITING")) {
            int readyCount = countReadyPlayers();
            if (readyCount >= 2 && allPlayersReady()) {
                state.gamePhase = "BUY";
            }
        }
        sendState();
    }

    public void setPlayerBuyPhaseDone(String playerName, boolean done) throws IOException {
        for (PlayerState p : state.players) {
            if (p.getPlayerName().equals(playerName)) {
                p.setBuyPhaseDone(done);
            }
        }
        sendState();
    }

    public boolean allPlayersReady() {
        if (state.players.isEmpty()) return false;
        for (PlayerState p : state.players) {
            if (!p.isReady()) return false;
        }
        return true;
    }

    private int countReadyPlayers() {
        int count = 0;
        for (PlayerState p : state.players) {
            if (p.isReady()) count++;
        }
        return count;
    }

    public String getCurrentTurn() {
        return state.turn;
    }

    public void nextTurn() throws IOException {
        if (!state.players.isEmpty()) {
            int currentIndex = -1;
            for (int i = 0; i < state.players.size(); i++) {
                if (state.players.get(i).getPlayerName().equals(state.turn)) {
                    currentIndex = i;
                    break;
                }
            }
            int nextIndex = (currentIndex + 1) % state.players.size();
            state.turn = state.players.get(nextIndex).getPlayerName();
        }
        sendState();
    }

    public boolean isMyTurn(String playerName) {
        return getCurrentTurn().equals(playerName);
    }

    public String getGamePhase() {
        return state.gamePhase;
    }

    public void setGamePhase(String phase) throws IOException {
        state.gamePhase = phase;
        sendState();
    }

    public boolean allBuyPhaseDone() {
        for (PlayerState p : state.players) {
            if (!p.isBuyPhaseDone()) {
                return false;
            }
        }
        return true;
    }

    public void resetBuyPhaseDone() throws IOException {
        for (PlayerState p : state.players) {
            p.setBuyPhaseDone(false);
        }
        sendState();
    }

    public void waitForUpdate() throws IOException {
        String line = in.readLine();
        if (line != null) {
            this.state = parseState(line);
        }
    }

    private void sendState() throws IOException {
        String stateStr = stateToString(state);
        out.write(stateStr + "\n");
        out.flush();
    }

    private LobbyState parseState(String content) {
        return LobbyParsingUtil.parseLobbyState(content);
    }

    private String stateToString(LobbyState state) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"players\":[");
        for (int i = 0; i < state.players.size(); i++) {
            PlayerState p = state.players.get(i);
            sb.append("{\"playerName\":\"").append(p.getPlayerName()).append("\",\"ready\":").append(p.isReady())
              .append(",\"buyPhaseDone\":").append(p.isBuyPhaseDone()).append("}");
            if (i < state.players.size()-1) sb.append(",");
        }
        sb.append("],\"turn\":\"").append(state.turn).append("\",\"gamePhase\":\"").append(state.gamePhase).append("\"}");
        return sb.toString();
    }
}
