import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Cli {
    private ShopBackend shopBackend;
    private Scanner scanner;
    private String lobbyName;
    private String playerName;
    private LobbyManager lobbyManager;

    public Cli() {
        this.shopBackend = new ShopBackend(10, 10, 1, 0);
        this.scanner = new Scanner(System.in);

        List<Friends> initialFreunde = new ArrayList<>();
        initialFreunde.add(generateDemoFriend("Schlierkamp"));
        initialFreunde.add(generateDemoFriend("Blankenstein"));
        initialFreunde.add(generateDemoFriend("Pieper"));

        List<Essen> initialEssen = new ArrayList<>();
        initialEssen.add(generateDemoEssen());
        initialEssen.add(generateDemoEssen());

        shopBackend.setShopFreunde(initialFreunde);
        shopBackend.setShopEssen(initialEssen);
    }

    public void lobby() {
        System.out.println("Super Auto Friends Lobby");
        System.out.print("Bitte geben Sie einen Lobby-Namen ein: ");
        this.lobbyName = scanner.nextLine();
        this.lobbyManager = new LobbyManager(lobbyName);

        System.out.print("Bitte geben Sie Ihren Spielernamen ein: ");
        this.playerName = scanner.nextLine();

        try {
            if (!lobbyManager.lobbyExists()) {
                lobbyManager.createLobby(playerName);
            } else {
                lobbyManager.joinLobby(playerName);
            }

            // Spieler ist ready
            lobbyManager.setPlayerReady(playerName, true);
            System.out.println("Lobby '" + this.lobbyName + "' betreten. Warte auf genügend Spieler...");

            // Warte bis mindestens 2 Spieler bereit und Phase != WAITING
            while (!lobbyManager.getGamePhase().equals("BUY")) {
                // Spiel startet erst, wenn mindestens 2 Spieler da und alle ready.
                Thread.sleep(1000);
            }

            System.out.println("Alle Spieler sind bereit! Spiel beginnt mit der Kaufphase.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        System.out.println("Willkommen in der Super Auto Pets CLI!");
        boolean running = true;
        while (running) {
            try {
                // Warte bis Phase BUY
                while (!lobbyManager.getGamePhase().equals("BUY")) {
                    System.out.println("Warte auf Start der Kaufphase...");
                    Thread.sleep(1000);
                }

                // Jede Runde Gold zurücksetzen
                shopBackend.setGold(10);

                System.out.println("Aktuelle Runde: " + shopBackend.getRunde() + " | Gold: " + shopBackend.getGold() +
                                   " | Leben: " + shopBackend.getLeben() + " | Wins: " + shopBackend.getWins());

                startBuyPhase();

                if (shopBackend.getTeam().isEmpty()) {
                    System.out.println("Keine Freunde im Team! Spiel Ende.");
                    running = false;
                    break;
                }

                // Nach Ende der Kaufphase warten, bis alle buyPhaseDone
                lobbyManager.setPlayerBuyPhaseDone(playerName, true);
                System.out.println("Warte, bis alle Spieler ihre Kaufphase beendet haben...");
                while (!lobbyManager.allBuyPhaseDone()) {
                    Thread.sleep(1000);
                }

                // Alle sind fertig mit Kaufphase, wechsle in FIGHT Phase
                // In einer echten Umgebung würde man prüfen, ob man "Host" ist. Hier setzen wir es einfach direkt:
                lobbyManager.setGamePhase("FIGHT");

                // Warte bis Phase FIGHT
                while (!lobbyManager.getGamePhase().equals("FIGHT")) {
                    Thread.sleep(500);
                }

                startFightPhase(generateEnemyTeam()); 

                // Nach Kampf wieder in BUY für nächste Runde
                // Setze alle buyPhaseDone zurück
                lobbyManager.resetBuyPhaseDone();
                // Erhöhe Runde im Backend
                shopBackend = new ShopBackend(shopBackend.getGold(), shopBackend.getLeben(), shopBackend.getRunde() + 1, shopBackend.getWins());
                shopBackend.setGold(10);

                // Neue Items für nächste Runde
                shopBackend.setShopFreunde(generateNewFreunde());
                shopBackend.setShopEssen(generateNewEssen());

                // Setze Phase zurück auf BUY
                lobbyManager.setGamePhase("BUY");

                System.out.println("Nächste Runde starten? (j/n)");
                String input = scanner.nextLine();
                if (!input.equalsIgnoreCase("j")) {
                    running = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                running = false;
            }
        }
        System.out.println("Spiel beendet. Auf Wiedersehen!");
    }

    private void startBuyPhase() {
        System.out.println("----- KAUFPHASE -----");
        try {
            // Warte, bis es dein Zug ist. (Optional, wenn man Rundenbasiertes Kaufen will)
            // Für dieses Beispiel können alle gleichzeitig in der Kaufphase agieren, da nur final buyPhaseDone zählt.
            // Wenn man Turn-basierte Kaufphase will, entkommenFreunden:
            // while (!lobbyManager.isMyTurn(playerName)) {
            //    System.out.println("Warte auf deinen Zug in der Kaufphase...");
            //    Thread.sleep(1000);
            // }

        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean inBuyPhase = true;
        while (inBuyPhase) {
            printShopStatus();
            printTeamStatus();

            System.out.println("Aktionen:");
            System.out.println("[1] Würfeln (Kosten: 1 Gold)");
            System.out.println("[2] Freund kaufen (Kosten: 3 Gold)");
            System.out.println("[3] Freund verkaufen (+1 Gold)");
            System.out.println("[4] Essen kaufen (Kosten: 3 Gold)");
            System.out.println("[5] Einfrieren");
            System.out.println("[6] Kaufphase beenden");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    shopBackend.rerollShop();
                    break;
                case "2":
                    System.out.println("Welchen Freund kaufen? (Index ab 0)");
                    int FreundIndex = readInt();
                    shopBackend.buyFriend(FreundIndex);
                    break;
                case "3":
                    System.out.println("Welchen Freund aus dem Team verkaufen? (Index ab 0)");
                    int sellIndex = readInt();
                    shopBackend.sellFriend(sellIndex);
                    break;
                case "4":
                    System.out.println("Welches Essen kaufen? (Index ab 0)");
                    int essenIndex = readInt();
                    System.out.println("Welcher Freund soll das Essen erhalten? (Index ab 0)");
                    int teamIndex = readInt();
                    shopBackend.buyItem(essenIndex, teamIndex);
                    break;
                case "5":
                    System.out.println("Freund einfrieren? (Index ab 0, -1 wenn keins)");
                    int freezeFreund = readInt();
                    System.out.println("Essen einfrieren? (Index ab 0, -1 wenn keins)");
                    int freezeEssen = readInt();
                    shopBackend.freezeItem(freezeFreund, freezeEssen);
                    break;
                case "6":
                    inBuyPhase = false;
                    // Hier setzen wir nicht turnWechsel, da alle gleichzeitig beenden können.
                    break;
                default:
                    System.out.println("Ungültige Aktion.");
                    break;
            }
        }
    }

    private void startFightPhase(List<Friends> enemyTeam) {
        System.out.println("----- KAMPFPHASE -----");
        // Alle führen Kampf lokal aus. In echtem Multiplayer müsste man das synchronisieren.
        FightBackend fightBackend = new FightBackend(shopBackend.getTeam(), enemyTeam);
        String result = fightBackend.startFight();
        System.out.println("Kampfergebnis: " + result);
        if (result.equals("A gewinnt")) {
            incrementWins();
        } else if (result.equals("B gewinnt")) {
            decrementLeben();
        }
    }

    private void incrementWins() {
        shopBackend = new ShopBackend(shopBackend.getGold(), shopBackend.getLeben(), shopBackend.getRunde(), shopBackend.getWins() + 1);
    }

    private void decrementLeben() {
        shopBackend = new ShopBackend(shopBackend.getGold(), shopBackend.getLeben() - 1, shopBackend.getRunde(), shopBackend.getWins());
    }

    private void printShopStatus() {
        System.out.println("** SHOP **");
        System.out.println("Freunde:");
        List<Friends> shopFreunde = shopBackend.getShopFreunde();
        for (int i = 0; i < shopFreunde.size(); i++) {
            Friends f = shopFreunde.get(i);
            if (f != null) {
                System.out.println("[" + i + "]: " + f.getName() + " (L:" + f.getLeben() + ", S:" + f.getSchaden() + ")");
            } else {
                System.out.println("[" + i + "]: leer");
            }
        }
        System.out.println("Essen:");
        List<Essen> shopEssen = shopBackend.getShopEssen();
        for (int i = 0; i < shopEssen.size(); i++) {
            Essen e = shopEssen.get(i);
            if (e != null) {
                System.out.println("[" + i + "]: Essen (+L:" + e.getLebensEffekt() + ", +S:" + e.getSchadensEffekt() + ")");
            } else {
                System.out.println("[" + i + "]: leer");
            }
        }
    }

    private void printTeamStatus() {
        System.out.println("** TEAM **");
        List<Friends> team = shopBackend.getTeam();
        for (int i = 0; i < team.size(); i++) {
            Friends f = team.get(i);
            System.out.println("[" + i + "]: " + f.getName() + " (L:" + f.getLeben() + ", S:" + f.getSchaden() + ")");
        }
    }

    private int readInt() {
        while (true) {
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.println("Bitte eine Zahl eingeben!");
            }
        }
    }

    private Friends generateDemoFriend(String name) {
        return new GenericFriend(name, 2, 2, "Kein Effekt");
    }

    private Essen generateDemoEssen() {
        return new BasicEssen(1, 1, "");
    }

    private List<Friends> generateEnemyTeam() {
        // Gegnerteam generieren - in echt müsste man hier andere Spieler abbilden.
        List<Friends> enemyTeam = new ArrayList<>();
        enemyTeam.add(generateDemoFriend("Schlierie"));
        enemyTeam.add(generateDemoFriend("Blanki"));
        enemyTeam.add(generateDemoFriend("Schlierie"));
        return enemyTeam;
    }

    private List<Friends> generateNewFreunde() {
        List<Friends> newFreunde = new ArrayList<>();
        newFreunde.add(generateDemoFriend("Schlierie"));
        newFreunde.add(generateDemoFriend("Blanki"));
        newFreunde.add(generateDemoFriend("Schlierie"));
        return newFreunde;
    }

    private List<Essen> generateNewEssen() {
        List<Essen> newEssen = new ArrayList<>();
        newEssen.add(generateDemoEssen());
        newEssen.add(generateDemoEssen());
        return newEssen;
    }
}
