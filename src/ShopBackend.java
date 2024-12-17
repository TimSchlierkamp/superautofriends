import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShopBackend {
    private int gold;
    private int leben;
    private int runde;
    private int wins;
    private static final int MAX_TEAM_SIZE = 5;
    private static final int REROLL_COST = 1;
    private static final int BUY_FRIEND_COST = 3;

    private List<Friends> team; 
    private List<Friends> shopTiere;
    private List<Essen> shopEssen;
    private List<Boolean> shopTiereGefroren;
    private List<Boolean> shopEssenGefroren;

    public ShopBackend(int startGold, int startLeben, int startRunde, int startWins) {
        this.gold = startGold;
        this.leben = startLeben;
        this.runde = startRunde;
        this.wins = startWins;
        this.team = new ArrayList<>();
        this.shopTiere = new ArrayList<>();
        this.shopEssen = new ArrayList<>();
        this.shopTiereGefroren = new ArrayList<>();
        this.shopEssenGefroren = new ArrayList<>();
    }

    public void rerollShop() {
        if (gold < REROLL_COST) {
            System.out.println("Nicht genug Gold zum Würfeln!");
            return;
        }
        gold -= REROLL_COST;
        System.out.println("Gold nach Würfeln: " + gold);
        for (int i = 0; i < shopTiere.size(); i++) {
            if (!shopTiereGefroren.get(i)) {
                Friends old = shopTiere.get(i);
                shopTiere.set(i, generateRandomFriend());
                System.out.println("Shop Tier [" + i + "] ersetzt: " + old.getName() + " -> " + shopTiere.get(i).getName());
            }
        }
        for (int i = 0; i < shopEssen.size(); i++) {
            if (!shopEssenGefroren.get(i)) {
                Essen old = shopEssen.get(i);
                shopEssen.set(i, generateRandomEssen());
                System.out.println("Shop Essen [" + i + "] ersetzt: +L:" + old.getLebensEffekt() + ", +S:" + old.getSchadensEffekt());
            }
        }
    }

    public void buyFriend(int index) {
        if (index < 0 || index >= shopTiere.size()) {
            System.out.println("Ungültiger Tier-Index.");
            return;
        }
        if (gold < BUY_FRIEND_COST) {
            System.out.println("Nicht genug Gold, um das Tier zu kaufen!");
            return;
        }
        if (team.size() >= MAX_TEAM_SIZE) {
            System.out.println("Team ist bereits voll!");
            return;
        }
        Friends chosenFriend = shopTiere.get(index);
        if (chosenFriend == null) {
            System.out.println("Kein Tier im angegebenen Shop-Slot.");
            return;
        }
        gold -= BUY_FRIEND_COST;
        team.add(chosenFriend);
        shopTiere.set(index, null);
        System.out.println("Gekauftes Tier: " + chosenFriend.getName() + ". Gold verbleibend: " + gold);
    }
     /**
     * Setzt den aktuellen Lebenswert.
     * @param leben Der neue Lebenswert.
     */
    public void setLeben(int leben) {
        this.leben = leben;
        System.out.println("Leben gesetzt auf: " + this.leben);
    }

    /**
     * Setzt die Anzahl der Siege.
     * @param wins Die neue Anzahl der Siege.
     */
    public void setWins(int wins) {
        this.wins = wins;
        System.out.println("Wins gesetzt auf: " + this.wins);
    }

    public void sellFriend(int index) {
        if (index < 0 || index >= team.size()) {
            System.out.println("Ungültiger Team-Index.");
            return;
        }
        Friends toSell = team.get(index);
        if (toSell == null) {
            System.out.println("Kein Tier an dieser Stelle.");
            return;
        }
        gold += 1;
        team.remove(index);
        System.out.println("Verkauftes Tier: " + toSell.getName() + ". Gold nach Verkauf: " + gold);
    }

    public void freezeItem(int tierIndex, int essenIndex) {
        if (tierIndex >= 0 && tierIndex < shopTiereGefroren.size()) {
            shopTiereGefroren.set(tierIndex, true);
            System.out.println("Tier im Shop [" + tierIndex + "] eingefroren.");
        }
        if (essenIndex >= 0 && essenIndex < shopEssenGefroren.size()) {
            shopEssenGefroren.set(essenIndex, true);
            System.out.println("Essen im Shop [" + essenIndex + "] eingefroren.");
        }
    }

    public void buyItem(int essenIndex, int teamIndex) {
        if (essenIndex < 0 || essenIndex >= shopEssen.size()) {
            System.out.println("Ungültiger Essens-Index.");
            return;
        }
        if (teamIndex < 0 || teamIndex >= team.size()) {
            System.out.println("Ungültiger Team-Index.");
            return;
        }
        if (gold < BUY_FRIEND_COST) {
            System.out.println("Nicht genug Gold, um das Essen zu kaufen!");
            return;
        }

        Essen chosenEssen = shopEssen.get(essenIndex);
        if (chosenEssen == null) {
            System.out.println("Kein Essen im angegebenen Shop-Slot.");
            return;
        }

        Friends target = team.get(teamIndex);
        target.setLeben(target.getLeben() + chosenEssen.getLebensEffekt());
        target.setSchaden(target.getSchaden() + chosenEssen.getSchadensEffekt());

        gold -= BUY_FRIEND_COST;
        shopEssen.set(essenIndex, null);
        System.out.println("Essen gekauft und angewendet auf " + target.getName() + ". Gold verbleibend: " + gold);
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getLeben() {
        return leben;
    }

    public void setRunde(int runde) {
        this.runde = runde;
    }

    public int getRunde() {
        return runde;
    }

    public int getWins() {
        return wins;
    }

    public List<Friends> getTeam() {
        return team;
    }

    public List<Friends> getShopTiere() {
        return shopTiere;
    }

    public List<Essen> getShopEssen() {
        return shopEssen;
    }

    public void setShopTiere(List<Friends> shopTiere) {
        this.shopTiere = new ArrayList<>(shopTiere);
        this.shopTiereGefroren.clear();
        for (int i = 0; i < shopTiere.size(); i++) {
            shopTiereGefroren.add(false);
        }
        System.out.println("Shop-Tiere initialisiert.");
    }

    public void setShopEssen(List<Essen> shopEssen) {
        this.shopEssen = new ArrayList<>(shopEssen);
        this.shopEssenGefroren.clear();
        for (int i = 0; i < shopEssen.size(); i++) {
            shopEssenGefroren.add(false);
        }
        System.out.println("Shop-Essen initialisiert.");
    }

    private Friends generateRandomFriend() {
        String[] namen = {"Ameise", "Fisch", "Biber"};
        Random rand = new Random();
        String name = namen[rand.nextInt(namen.length)];
        int leben = rand.nextInt(3) + 1;
        int schaden = rand.nextInt(3) + 1;
        String effekt = "Kein Effekt";
        return new GenericFriend(name, leben, schaden, effekt);
    }

    private Essen generateRandomEssen() {
        Random rand = new Random();
        int lebensEffekt = rand.nextInt(2) + 1; 
        int schadensEffekt = rand.nextInt(2) + 1;
        String beschwoeren = "";
        return new BasicEssen(lebensEffekt, schadensEffekt, beschwoeren);
    }

    /**
     * Druckt den aktuellen Shop-Status zur Debugging-Zwecken.
     */
    public void printShopStatus() {
        System.out.println("----- Aktueller Shop-Status -----");
        System.out.println("Gold: " + gold);
        System.out.println("Leben: " + leben);
        System.out.println("Runde: " + runde);
        System.out.println("Wins: " + wins);

        System.out.println("Shop-Tiere:");
        for (int i = 0; i < shopTiere.size(); i++) {
            Friends f = shopTiere.get(i);
            String status = shopTiereGefroren.get(i) ? " (Eingefroren)" : "";
            if (f != null) {
                System.out.println("[" + i + "]: " + f.getName() + " (L:" + f.getLeben() + ", S:" + f.getSchaden() + ")" + status);
            } else {
                System.out.println("[" + i + "]: leer" + status);
            }
        }

        System.out.println("Shop-Essen:");
        for (int i = 0; i < shopEssen.size(); i++) {
            Essen e = shopEssen.get(i);
            String status = shopEssenGefroren.get(i) ? " (Eingefroren)" : "";
            if (e != null) {
                System.out.println("[" + i + "]: Essen (+L:" + e.getLebensEffekt() + ", +S:" + e.getSchadensEffekt() + ")" + status);
            } else {
                System.out.println("[" + i + "]: leer" + status);
            }
        }

        System.out.println("Team:");
        for (int i = 0; i < team.size(); i++) {
            Friends f = team.get(i);
            System.out.println("[" + i + "]: " + f.getName() + " (L:" + f.getLeben() + ", S:" + f.getSchaden() + ")");
        }

        System.out.println("---------------------------------");
    }

    public void updateShop(List<Friends> newTiere, List<Essen> newEssen) {
        // Aktualisiere nur nicht-gefrorene Shop-Tiere
        for (int i = 0; i < shopTiere.size(); i++) {
            if (!shopTiereGefroren.get(i)) {
                shopTiere.set(i, i < newTiere.size() ? newTiere.get(i) : generateRandomFriend());
            }
        }
        // Aktualisiere nur nicht-gefrorene Shop-Essen
        for (int i = 0; i < shopEssen.size(); i++) {
            if (!shopEssenGefroren.get(i)) {
                shopEssen.set(i, i < newEssen.size() ? newEssen.get(i) : generateRandomEssen());
            }
        }
        System.out.println("Shop für neue Runde aktualisiert!");
    }
}
