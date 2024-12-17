import java.util.ArrayList;
import java.util.List;

class LobbyParsingUtil {
    public static LobbyState parseLobbyState(String content, String ownPlayerName, LobbyState currentState) {
        String trimmed = content.trim();
        int ps = trimmed.indexOf("\"players\":");
        int start = trimmed.indexOf("[", ps);
        int end = trimmed.indexOf("]", start);
        String playersSection = trimmed.substring(start + 1, end).trim();
        List<PlayerState> players = new ArrayList<>();
        if (!playersSection.isEmpty()) {
            String[] ents = playersSection.split("\\},\\{");
            for (String e : ents) {
                String pe = e.trim();
                if (!pe.startsWith("{")) pe = "{" + pe;
                if (!pe.endsWith("}")) pe = pe + "}";
                PlayerState parsedPlayer = parsePlayer(pe);
                if (parsedPlayer.getPlayerName().equals(ownPlayerName)) {
                    // Bewahren Sie die lokalen Flags
                    PlayerState localPlayer = findPlayerInState(currentState, ownPlayerName);
                    if (localPlayer != null) {
                        parsedPlayer.setBuyPhaseDone(localPlayer.isBuyPhaseDone());
                        parsedPlayer.setReady(localPlayer.isReady());
                    }
                }
                players.add(parsedPlayer);
            }
        }

        String turn = parseStringField(trimmed, "turn");
        String gamePhase = parseStringField(trimmed, "gamePhase");

        System.out.println("Parsed LobbyState: gamePhase=" + gamePhase + ", turn=" + turn + ", players=" + players.size());

        return new LobbyState(players, turn, gamePhase);
    }

    private static PlayerState findPlayerInState(LobbyState state, String playerName) {
        for (PlayerState p : state.players) {
            if (p.getPlayerName().equals(playerName)) {
                return p;
            }
        }
        return null;
    }

    private static PlayerState parsePlayer(String json) {
        String name = parseStringField(json, "playerName");
        boolean ready = parseBooleanField(json, "ready");
        boolean buyDone = parseBooleanField(json, "buyPhaseDone");
        PlayerState p = new PlayerState(name);
        p.setReady(ready);
        p.setBuyPhaseDone(buyDone);
        return p;
    }

    private static String parseStringField(String json, String fieldName) {
        String pat = "\"" + fieldName + "\":\"";
        int idx = json.indexOf(pat);
        if (idx < 0) return "";
        int start = idx + pat.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private static boolean parseBooleanField(String json, String fieldName) {
        String pat = "\"" + fieldName + "\":";
        int idx = json.indexOf(pat);
        if (idx < 0) return false;
        int start = idx + pat.length();
        int end = json.indexOf(",", start);
        if (end < 0) end = json.indexOf("}", start);
        String val = json.substring(start, end).trim();
        return Boolean.parseBoolean(val);
    }
}
