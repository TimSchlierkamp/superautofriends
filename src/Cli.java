import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Cli {
    private ShopBackend shopBackend;
    private Scanner scanner;

    public Cli() {
        this.shopBackend = new ShopBackend(10, 10, 1, 0);
        this.scanner = new Scanner(System.in);

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

    public void run() {
        System.out.println("Willkommen in der Super Auto Pets CLI!");
        boolean running = true;
        while (running) {
            // Zu Beginn jeder Runde Gold auf 10 setzen
            shopBackend.setGold(10);

            System.out.println("Aktuelle Runde: " + shopBackend.getRunde() + " | Gold: " + shopBackend.getGold() +
                               " | Leben: " + shopBackend.getLeben() + " | Wins: " + shopBackend.getWins());
            startBuyPhase();

            if (shopBackend.getTeam().isEmpty()) {
                System.out.println("Keine Tiere im Team! Spiel Ende.");
                running = false;
                break;
            }

            // Gegner-Team erstellen
            List<Friends> enemyTeam = new ArrayList<>();
            enemyTeam.add(generateDemoFriend("Schlierie"));
            enemyTeam.add(generateDemoFriend("Blanki"));
            enemyTeam.add(generateDemoFriend("Schlierie"));
            startFightPhase(enemyTeam);

            int newRound = shopBackend.getRunde() + 1;

            // Neues ShopBackend für nächste Runde - Stats übernehmen
            shopBackend = new ShopBackend(shopBackend.getGold(), shopBackend.getLeben(), newRound, shopBackend.getWins());

            // Gleich wieder Gold auf 10 setzen für nächste Kaufphase
            shopBackend.setGold(10);

            List<Friends> newTiere = new ArrayList<>();
            newTiere.add(generateDemoFriend("Schlierie"));
            newTiere.add(generateDemoFriend("Blanki"));
            newTiere.add(generateDemoFriend("Schlierie"));

            List<Essen> newEssen = new ArrayList<>();
            newEssen.add(generateDemoEssen());
            newEssen.add(generateDemoEssen());

            shopBackend.setShopTiere(newTiere);
            shopBackend.setShopEssen(newEssen);

            System.out.println("Nächste Runde starten? (j/n)");
            String input = scanner.nextLine();
            if (!input.equalsIgnoreCase("j")) {
                running = false;
            }
        }
        System.out.println("Spiel beendet. Auf Wiedersehen!");
    }

    private void startBuyPhase() {
        System.out.println("----- KAUFPHASE -----");
        // Gold zu Beginn der Kaufphase sicher auf 10 setzen
        shopBackend.setGold(10);

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
            System.out.println("[6] Kaufphase beenden und in die Kampfphase gehen");
            
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
            incrementWins();
        } else if (result.equals("B gewinnt")) {
            decrementLeben();
        } else {
            // Unentschieden -> Keine Änderung
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
        System.out.println("Tiere:");
        List<Friends> shopTiere = shopBackend.getShopTiere();
        for (int i = 0; i < shopTiere.size(); i++) {
            Friends f = shopTiere.get(i);
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
}
