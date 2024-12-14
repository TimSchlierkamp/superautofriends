import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Cli {
    private ShopBackend shopBackend;
    private Scanner scanner;
    private NetworkLobbyManager networkLobbyManager;
    private String playerName;

    public Cli() {
        this.shopBackend = new ShopBackend(10, 10, 1, 0);
        this.scanner = new Scanner(System.in);
        this.networkLobbyManager = new NetworkLobbyManager();

        // Initiale Shop Items
        List<Friends> initialTiere = new ArrayList<>();
        initialTiere.add(generateDemoFriend("Schlierie"));
        initialTiere.add(generateDemoFriend("Blanki"));
        initialTiere.add(generateDemoFriend("Schlierie"));

        List<Essen> initialEssen = new ArrayList<>();
        initialEssen.add(generateDemoEssen());
        initialEssen.add(generateDemoEssen());

        shopBackend.setShopTiere(initialTiere);
        shopBackend.setShopEssen(initialEssen);
    }

    public void lobby() {
        System.out.println("Super Auto Friends Lobby");
        System.out.print("Bist du Host (h) oder Client (c)? ");
        String role = scanner.nextLine();

        System.out.print("Bitte geben Sie Ihren Spielernamen ein: ");
        this.playerName = scanner.nextLine();

        try {
            if (role.equalsIgnoreCase("h")) {
                System.out.print("Auf welchem Port soll gehostet werden? ");
                int port = Integer.parseInt(scanner.nextLine());
                networkLobbyManager.lobbyHost(port, playerName);
            } else {
                System.out.print("Bitte IP des Hosts eingeben: ");
                String hostIP = scanner.nextLine();
                System.out.print("Bitte Port des Hosts eingeben: ");
                int port = Integer.parseInt(scanner.nextLine());
                networkLobbyManager.lobbyJoin(hostIP, port, playerName);
            }
            // Spieler ist ready
            networkLobbyManager.setPlayerReady(playerName, true);
            System.out.println("Warte, bis Spiel beginnt (mind. 2 Spieler müssen bereit sein)...");

            // Warte bis BUY Phase erreicht ist
            while (!networkLobbyManager.getGamePhase().equals("BUY")) {
                networkLobbyManager.waitForUpdate();
                Thread.sleep(500);
            }
            System.out.println("Spiel startet in Kaufphase!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        System.out.println("Willkommen in der Super Auto Pets CLI!");
        boolean running = true;
        while (running) {
            try {
                // Warte bis BUY Phase
                while (!networkLobbyManager.getGamePhase().equals("BUY")) {
                    networkLobbyManager.waitForUpdate();
                    Thread.sleep(500);
                }

                // Jede Runde Gold zurücksetzen
                shopBackend.setGold(10);
                System.out.println("Aktuelle Runde: " + shopBackend.getRunde() + " | Gold: " + shopBackend.getGold() +
                                   " | Leben: " + shopBackend.getLeben() + " | Wins: " + shopBackend.getWins());

                startBuyPhase();

                if (shopBackend.getTeam().isEmpty()) {
                    System.out.println("Keine Tiere im Team! Spiel Ende.");
                    running = false;
                    break;
                }

                // Kaufphase beendet
                networkLobbyManager.setPlayerBuyPhaseDone(playerName, true);
                System.out.println("Warte, bis alle Spieler ihre Kaufphase beendet haben...");
                while (!networkLobbyManager.allBuyPhaseDone()) {
                    networkLobbyManager.waitForUpdate();
                    Thread.sleep(500);
                }

                // Alle sind fertig mit Kaufphase, wechsle in FIGHT Phase
                networkLobbyManager.setGamePhase("FIGHT");
                while (!networkLobbyManager.getGamePhase().equals("FIGHT")) {
                    networkLobbyManager.waitForUpdate();
                    Thread.sleep(500);
                }

                startFightPhase(generateEnemyTeam());

                // Nach Kampf wieder in BUY für nächste Runde
                networkLobbyManager.resetBuyPhaseDone();
                shopBackend = new ShopBackend(shopBackend.getGold(), shopBackend.getLeben(), shopBackend.getRunde() + 1, shopBackend.getWins());
                shopBackend.setGold(10);

                // Neue Items für nächste Runde
                shopBackend.setShopTiere(generateNewTiere());
                shopBackend.setShopEssen(generateNewEssen());

                // Setze Phase zurück auf BUY
                networkLobbyManager.setGamePhase("BUY");

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
        boolean inBuyPhase = true;
        while (inBuyPhase) {
            printShopStatus();
            printTeamStatus();

            System.out.println("Aktionen:");
            System.out.println("[1] Würfeln (Kosten: 1 Gold)");
            System.out.println("[2] Tier kaufen (Kosten: 3 Gold)");
            System.out.println("[3] Tier verkaufen (+1 Gold)");
            System.out.println("[4] Essen kaufen (Kosten: 3 Gold)");
            System.out.println("[5] Einfrieren");
            System.out.println("[6] Kaufphase beenden");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    shopBackend.rerollShop();
                    break;
                case "2":
                    System.out.println("Welches Tier kaufen? (Index ab 0)");
                    int tierIndex = readInt();
                    shopBackend.buyFriend(tierIndex);
                    break;
                case "3":
                    System.out.println("Welches Tier aus dem Team verkaufen? (Index ab 0)");
                    int sellIndex = readInt();
                    shopBackend.sellFriend(sellIndex);
                    break;
                case "4":
                    System.out.println("Welches Essen kaufen? (Index ab 0)");
                    int essenIndex = readInt();
                    System.out.println("Welches Tier soll das Essen erhalten? (Index ab 0)");
                    int teamIndex = readInt();
                    shopBackend.buyItem(essenIndex, teamIndex);
                    break;
                case "5":
                    System.out.println("Tier einfrieren? (Index ab 0, -1 wenn keins)");
                    int freezeTier = readInt();
                    System.out.println("Essen einfrieren? (Index ab 0, -1 wenn keins)");
                    int freezeEssen = readInt();
                    shopBackend.freezeItem(freezeTier, freezeEssen);
                    break;
                case "6":
                    inBuyPhase = false;
                    break;
                default:
                    System.out.println("Ungültige Aktion.");
                    break;
            }
        }
    }

    private void startFightPhase(List<Friends> enemyTeam) {
        System.out.println("----- KAMPFPHASE -----");
        FightBackend fightBackend = new FightBackend(shopBackend.getTeam(), enemyTeam);
        String result = fightBackend.startFight();
        System.out.println("Kampfergebnis: " + result);
        if (result.equals("A gewinnt")) {
            shopBackend = new ShopBackend(shopBackend.getGold(), shopBackend.getLeben(), shopBackend.getRunde(), shopBackend.getWins() + 1);
            System.out.println("Sie haben gewonnen! Wins: " + shopBackend.getWins());
        } else if (result.equals("B gewinnt")) {
            shopBackend = new ShopBackend(shopBackend.getGold(), shopBackend.getLeben() - 1, shopBackend.getRunde(), shopBackend.getWins());
            System.out.println("Sie haben verloren! Leben: " + shopBackend.getLeben());
        } else {
            System.out.println("Das Spiel endet unentschieden!");
        }
    }

    private void printShopStatus() {
        shopBackend.printShopStatus();
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
                System.out.println("Bitte eine gültige Zahl eingeben!");
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
        List<Friends> enemyTeam = new ArrayList<>();
        enemyTeam.add(generateDemoFriend("Schlierie"));
        enemyTeam.add(generateDemoFriend("Blanki"));
        enemyTeam.add(generateDemoFriend("Schlierie"));
        return enemyTeam;
    }

    private List<Friends> generateNewTiere() {
        List<Friends> newTiere = new ArrayList<>();
        newTiere.add(generateDemoFriend("Schlierie"));
        newTiere.add(generateDemoFriend("Blanki"));
        newTiere.add(generateDemoFriend("Schlierie"));
        return newTiere;
    }

    private List<Essen> generateNewEssen() {
        List<Essen> newEssen = new ArrayList<>();
        newEssen.add(generateDemoEssen());
        newEssen.add(generateDemoEssen());
        return newEssen;
    }
}
//test