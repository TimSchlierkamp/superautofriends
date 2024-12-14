import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class NetworkLobbyManager {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    private LobbyState state;

    private boolean isHost;

    /**
     * Host-Funktionalität: Startet einen ServerSocket und wartet auf einen Client.
     */
    public void lobbyHost(int port, String playerName) throws IOException {
        isHost = true;
        // Initialer State
        List<PlayerState> players = new ArrayList<>();
        PlayerState p = new PlayerState(playerName);
        p.setReady(false);
        p.setBuyPhaseDone(false);
        this.state = new LobbyState(players, playerName, "WAITING");
        this.state.players.add(p);

        System.out.println("Host: Starten des Lobby-Hosts mit Spielername '" + playerName + "'.");
        ServerSocket server = new ServerSocket(port);
        System.out.println("Host: Warte auf Verbindung des anderen Spielers auf Port " + port + "...");
        this.socket = server.accept();
        System.out.println("Host: Spieler verbunden!");

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        // Sende initialen State an Client
        sendState();
        System.out.println("Host: Initialer Lobby-State gesendet.");

        // Warte auf Rückmeldung des Clients (Client sendet aktualisierten State)
        waitForUpdate();
        System.out.println("Host: Lobby-State vom Client empfangen.");
        printLobbyState();

        // Host setzt seinen Ready-Status auf true
        setPlayerReady(playerName, true);
    }

    /**
     * Client-Funktionalität: Verbindet sich mit dem Host und aktualisiert den Lobby-State.
     */
    public void lobbyJoin(String hostAddress, int port, String playerName) throws IOException {
        isHost = false;
        System.out.println("Client: Versuche, eine Verbindung zum Host " + hostAddress + ":" + port + " herzustellen...");
        this.socket = new Socket(hostAddress, port);
        System.out.println("Client: Mit Host verbunden!");

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        // Warte auf State vom Host
        String stateStr = in.readLine();
        this.state = parseState(stateStr);
        System.out.println("Client: Lobby-State vom Host empfangen.");
        printLobbyState();

        // Füge unseren Spieler hinzu und sende aktualisierten State zurück
        PlayerState p = new PlayerState(playerName);
        p.setReady(false);
        p.setBuyPhaseDone(false);
        state.players.add(p);
        System.out.println("Client: Füge Spieler '" + playerName + "' zur Lobby hinzu.");
        sendState();
        System.out.println("Client: Aktualisierter Lobby-State gesendet.");
    }

    /**
     * Setzt einen Spieler auf ready und synchronisiert mit der Gegenstelle.
     */
    public void setPlayerReady(String playerName, boolean ready) throws IOException {
        boolean playerFound = false;
        for (PlayerState p : state.players) {
            if (p.getPlayerName().equals(playerName)) {
                p.setReady(ready);
                playerFound = true;
                break;
            }
        }
        if (!playerFound) {
            System.out.println("Warnung: Spieler '" + playerName + "' nicht in der Lobby gefunden.");
        }

        // Überprüfen, ob alle Spieler bereit sind
        if (state.gamePhase.equals("WAITING")) {
            if (allPlayersReady()) {
                state.gamePhase = "BUY";
                System.out.println("Lobby: Alle Spieler sind bereit. Spielphase auf 'BUY' gesetzt.");
            }
        }

        sendState();
        System.out.println("Lobby: Player ready aktualisiert und State gesendet.");
        printLobbyState();
    }

    public void setPlayerBuyPhaseDone(String playerName, boolean done) throws IOException {
        for (PlayerState p : state.players) {
            if (p.getPlayerName().equals(playerName)) {
                p.setBuyPhaseDone(done);
            }
        }
        sendState();
        System.out.println("Lobby: BuyPhaseDone aktualisiert und State gesendet.");
        printLobbyState();
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
            System.out.println("Lobby: Nächster Zug von '" + state.turn + "'.");
        }
        sendState();
        printLobbyState();
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
        System.out.println("Lobby: Spielphase auf '" + phase + "' gesetzt.");
        printLobbyState();
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
        System.out.println("Lobby: BuyPhaseDone für alle Spieler zurückgesetzt und State gesendet.");
        printLobbyState();
    }

    /**
     * Wartet auf ein Update des Lobby-States von der Gegenstelle.
     */
    public void waitForUpdate() throws IOException {
        String line = in.readLine();
        if (line != null) {
            this.state = parseState(line);
            System.out.println("Lobby: Neuer Lobby-State empfangen.");
            printLobbyState();
        }
    }

    /**
     * Sendet den aktuellen Lobby-State an die Gegenstelle.
     */
    private void sendState() throws IOException {
        String stateStr = stateToString(state);
        out.write(stateStr + "\n");
        out.flush();
    }

    /**
     * Parsen des Lobby-States von einem JSON-ähnlichen String.
     */
    private LobbyState parseState(String content) {
        return LobbyParsingUtil.parseLobbyState(content);
    }

    /**
     * Konvertiert den Lobby-State zu einem JSON-ähnlichen String.
     */
    private String stateToString(LobbyState state) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"players\":[");
        for (int i = 0; i < state.players.size(); i++) {
            PlayerState p = state.players.get(i);
            sb.append("{\"playerName\":\"").append(p.getPlayerName()).append("\",\"ready\":").append(p.isReady())
              .append(",\"buyPhaseDone\":").append(p.isBuyPhaseDone()).append("}");
            if (i < state.players.size() - 1) sb.append(",");
        }
        sb.append("],\"turn\":\"").append(state.turn).append("\",\"gamePhase\":\"").append(state.gamePhase).append("\"}");
        return sb.toString();
    }

    /**
     * Druckt den aktuellen Lobby-State zur Debugging-Zwecken.
     */
    public void printLobbyState() {
        System.out.println("----- Aktueller Lobby-State -----");
        System.out.println("Game Phase: " + state.gamePhase);
        System.out.println("Turn: " + state.turn);
        System.out.println("Spieler:");
        for (PlayerState p : state.players) {
            System.out.println(" - " + p.getPlayerName() + " | Ready: " + p.isReady() + " | BuyPhaseDone: " + p.isBuyPhaseDone());
        }
        System.out.println("---------------------------------");
    }
}
