import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LobbyManager {
    private String lobbyName;
    private File lobbyFile;

    public LobbyManager(String lobbyName) {
        this.lobbyName = lobbyName;
        this.lobbyFile = new File(lobbyName + ".lobby");
    }

    public boolean lobbyExists() {
        return lobbyFile.exists();
    }

    public void createLobby(String playerName) throws IOException {
        List<PlayerState> players = new ArrayList<>();
        PlayerState p = new PlayerState(playerName);
        p.setReady(false);
        p.setBuyPhaseDone(false);
        LobbyState state = new LobbyState(players, playerName, "WAITING");
        state.players.add(p);
        writeLobbyState(state);
    }

    public void joinLobby(String playerName) throws IOException {
        LobbyState state = readLobbyState();
        boolean found = false;
        for (PlayerState pl : state.players) {
            if (pl.getPlayerName().equals(playerName)) {
                found = true;
                break;
            }
        }
        if (!found) {
            PlayerState p = new PlayerState(playerName);
            p.setReady(false);
            p.setBuyPhaseDone(false);
            state.players.add(p);
            writeLobbyState(state);
        }
    }

    public void setPlayerReady(String playerName, boolean ready) throws IOException {
        LobbyState state = readLobbyState();
        for (PlayerState p : state.players) {
            if (p.getPlayerName().equals(playerName)) {
                p.setReady(ready);
            }
        }
        // Wenn Spiel im WAITING Modus ist und mind. 2 Spieler bereit sind sowie alle bereit, dann Phase auf BUY
        if (state.gamePhase.equals("WAITING")) {
            int readyCount = countReadyPlayers(state);
            if (readyCount >= 2 && allPlayersReady(state)) {
                state.gamePhase = "BUY";
            }
        }
        writeLobbyState(state);
    }

    public void setPlayerBuyPhaseDone(String playerName, boolean done) throws IOException {
        LobbyState state = readLobbyState();
        for (PlayerState p : state.players) {
            if (p.getPlayerName().equals(playerName)) {
                p.setBuyPhaseDone(done);
            }
        }
        writeLobbyState(state);
    }

    public boolean allPlayersReady() throws IOException {
        LobbyState state = readLobbyState();
        return allPlayersReady(state);
    }

    private boolean allPlayersReady(LobbyState state) {
        if (state.players.isEmpty()) return false;
        for (PlayerState p : state.players) {
            if (!p.isReady()) return false;
        }
        return true;
    }

    private int countReadyPlayers(LobbyState state) {
        int count = 0;
        for (PlayerState p : state.players) {
            if (p.isReady()) count++;
        }
        return count;
    }

    public String getCurrentTurn() throws IOException {
        LobbyState state = readLobbyState();
        return state.turn;
    }

    public void nextTurn() throws IOException {
        LobbyState state = readLobbyState();
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
            writeLobbyState(state);
        }
    }

    public boolean isMyTurn(String playerName) throws IOException {
        return getCurrentTurn().equals(playerName);
    }

    public String getGamePhase() throws IOException {
        LobbyState state = readLobbyState();
        return state.gamePhase;
    }

    public void setGamePhase(String phase) throws IOException {
        LobbyState state = readLobbyState();
        state.gamePhase = phase;
        writeLobbyState(state);
    }

    public boolean allBuyPhaseDone() throws IOException {
        LobbyState state = readLobbyState();
        for (PlayerState p : state.players) {
            if (!p.isBuyPhaseDone()) {
                return false;
            }
        }
        return true;
    }

    public void resetBuyPhaseDone() throws IOException {
        LobbyState state = readLobbyState();
        for (PlayerState p : state.players) {
            p.setBuyPhaseDone(false);
        }
        writeLobbyState(state);
    }

    private LobbyState readLobbyState() throws IOException {
        if (!lobbyFile.exists()) {
            throw new IOException("Lobby-Datei exisFreundt nicht.");
        }
        try (BufferedReader br = new BufferedReader(new FileReader(lobbyFile))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return parseLobbyState(sb.toString());
        }
    }

    private void writeLobbyState(LobbyState state) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(lobbyFile))) {
            bw.write(stateToString(state));
        }
    }

    private LobbyState parseLobbyState(String content) {
        String trimmed = content.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw new RuntimeException("Ungültiges Lobby-Format: Kein korrektes JSON-ähnliches Objekt.");
        }

        int playersStart = trimmed.indexOf("\"players\":");
        if (playersStart < 0) {
            throw new RuntimeException("Ungültiges Format: Kein 'players' Feld.");
        }
        int bracketOpen = trimmed.indexOf("[", playersStart);
        int bracketClose = trimmed.indexOf("]", bracketOpen);
        if (bracketOpen < 0 || bracketClose < 0 || bracketClose < bracketOpen) {
            throw new RuntimeException("Ungültiges Format bei 'players': Fehlende eckige Klammern.");
        }

        String playersSection = trimmed.substring(bracketOpen+1, bracketClose).trim();
        List<PlayerState> players = new ArrayList<>();
        if (!playersSection.isEmpty()) {
            String[] playerEntries = playersSection.split("\\},\\{");
            for (int i = 0; i < playerEntries.length; i++) {
                String pe = playerEntries[i].trim();
                if (!pe.startsWith("{")) pe = "{" + pe;
                if (!pe.endsWith("}")) pe = pe + "}";
                PlayerState p = parsePlayerObject(pe);
                players.add(p);
            }
        }

        String turn = parseStringField(trimmed, "turn");
        String gamePhase;
        if (trimmed.contains("\"gamePhase\":")) {
            // gamePhase normal parsen
            gamePhase = parseStringField(trimmed, "gamePhase");
        } else {
            // Falls kein gamePhase-Feld vorhanden ist, default auf WAITING
            gamePhase = "WAITING";
        }

        return new LobbyState(players, turn, gamePhase);
    }

    private PlayerState parsePlayerObject(String jsonPlayer) {
        String name = parseStringField(jsonPlayer, "playerName");
        boolean ready = parseBooleanField(jsonPlayer, "ready");
        boolean buyDone = false;
        if (jsonPlayer.contains("\"buyPhaseDone\":")) {
            buyDone = parseBooleanField(jsonPlayer, "buyPhaseDone");
        }

        PlayerState p = new PlayerState(name);
        p.setReady(ready);
        p.setBuyPhaseDone(buyDone);
        return p;
    }

    private String parseStringField(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\":";
        int idx = json.indexOf(pattern);
        if (idx < 0) {
            throw new RuntimeException("Feld '" + fieldName + "' nicht gefunden in: " + json);
        }
        int startQuote = json.indexOf("\"", idx + pattern.length());
        if (startQuote < 0) {
            throw new RuntimeException("Stringfeld '" + fieldName + "' Wert nicht gefunden.");
        }
        int endQuote = json.indexOf("\"", startQuote+1);
        if (endQuote < 0) {
            throw new RuntimeException("Stringfeld '" + fieldName + "' Wert nicht korrekt abgeschlossen.");
        }
        return json.substring(startQuote+1, endQuote);
    }

    private boolean parseBooleanField(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\":";
        int idx = json.indexOf(pattern);
        if (idx < 0) {
            throw new RuntimeException("Feld '" + fieldName + "' nicht gefunden in: " + json);
        }
        int startVal = idx + pattern.length();
        int comma = json.indexOf(",", startVal);
        int brace = json.indexOf("}", startVal);
        int endVal;
        if (comma < 0 && brace < 0) {
            throw new RuntimeException("Booleanfeld '" + fieldName + "' nicht korrekt abgeschlossen.");
        } else if (comma < 0) {
            endVal = brace;
        } else if (brace < 0) {
            endVal = comma;
        } else {
            endVal = Math.min(comma, brace);
        }
        String valStr = json.substring(startVal, endVal).trim();
        if (!valStr.equals("true") && !valStr.equals("false")) {
            throw new RuntimeException("Booleanfeld '" + fieldName + "' hat keinen booleschen Wert: " + valStr);
        }
        return Boolean.parseBoolean(valStr);
    }

    private String stateToString(LobbyState state) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n\"players\":[");
        for (int i = 0; i < state.players.size(); i++) {
            PlayerState p = state.players.get(i);
            sb.append("{\"playerName\":\"").append(p.getPlayerName()).append("\",\"ready\":").append(p.isReady())
              .append(",\"buyPhaseDone\":").append(p.isBuyPhaseDone()).append("}");
            if (i < state.players.size()-1) sb.append(",");
        }
        sb.append("],\n\"turn\":\"").append(state.turn).append("\",\n\"gamePhase\":\"").append(state.gamePhase).append("\"\n}");
        return sb.toString();
    }
}
