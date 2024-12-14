
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Backend für das Kauf-Menü (Shop-Phase).
 * Hier werden alle Logiken für das Würfeln (Neuerstellen von Tieren im Shop),
 * Kaufen, Verkaufen, Einfrieren, sowie die Darstellung von Nutzer-Stats und Shop-Zonen implementiert.
 */
public class ShopBackend {
    private int gold;
    private int leben;
    private int runde;
    private int wins;
    private static final int MAX_TEAM_SIZE = 5;
    private static final int REROLL_COST = 1;
    private static final int BUY_FRIEND_COST = 3;
    // Die tatsächliche Kosten für den Verkauf oder andere Aktionen könnten variieren.
    // Hier wird angenommen, dass beim Verkauf eines Tieres Gold = Level (oder ein fixer Wert) gutgeschrieben wird.
    // Level ist aktuell nicht in Friends definiert. Man könnte Level separat tracken.
    
    private List<Friends> team;           // Kampf-Setup des Spielers (max 5 Tiere)
    private List<Friends> shopTiere;      // Verfügbare Tiere im Shop
    private List<Essen> shopEssen;        // Verfügbares Essen im Shop
    private List<Boolean> shopTiereGefroren; // Welche Tiere im Shop sind eingefroren
    private List<Boolean> shopEssenGefroren; // Welche Essen im Shop sind eingefroren

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

    /**
     * Aktualisiert den Shop mit neuen Tieren und Essen, außer die, die eingefroren sind.
     * Kostet den Spieler REROLL_COST Gold.
     */
    public void rerollShop() {
        if (gold < REROLL_COST) {
            System.out.println("Nicht genug Gold zum Würfeln!");
            return;
        }
        gold -= REROLL_COST;
        // Nur die nicht-gefrorenen Slots neu füllen
        for (int i = 0; i < shopTiere.size(); i++) {
            if (!shopTiereGefroren.get(i)) {
                shopTiere.set(i, generateRandomFriend());
            }
        }
        for (int i = 0; i < shopEssen.size(); i++) {
            if (!shopEssenGefroren.get(i)) {
                shopEssen.set(i, generateRandomEssen());
            }
        }
    }

    /**
     * Kauft ein Tier aus dem Shop und fügt es dem Team hinzu, falls Platz.
     * Kostet den Spieler BUY_FRIEND_COST Gold.
     * @param index Index des Tieres im Shop
     */
    public void buyFriend(int index) {
        if (index < 0 || index >= shopTiere.size()) {
            System.out.println("Ungültiger Index.");
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
        // Entferne das Tier aus dem Shop, der Slot könnte beim nächsten Reroll neu befüllt werden
        shopTiere.set(index, null);
    }

    /**
     * Verkauft ein Tier aus dem Team.
     * Gibt dem Spieler Gold zurück (z. B. in Höhe des 'Levels' des Tieres).
     * Da Level nicht definiert ist, wird hier fiktiv 1 als Rückgabewert genommen.
     * @param index Index des Tieres im Team
     */
    public void sellFriend(int index) {
        if (index < 0 || index >= team.size()) {
            System.out.println("Ungültiger Index im Team.");
            return;
        }
        Friends toSell = team.get(index);
        if (toSell == null) {
            System.out.println("Kein Tier an dieser Position.");
            return;
        }
        // Beispielhaftes Gold-Refund: 1 Gold pro Verkauf
        gold += 1;
        team.remove(index);
    }

    /**
     * Friert ein Tier oder Essen im Shop ein, damit es beim nächsten Würfeln nicht ersetzt wird.
     * @param tierIndex Index des Tieres (wenn -1, dann ignorieren)
     * @param essenIndex Index des Essens (wenn -1, dann ignorieren)
     */
    public void freezeItem(int tierIndex, int essenIndex) {
        if (tierIndex >= 0 && tierIndex < shopTiereGefroren.size()) {
            shopTiereGefroren.set(tierIndex, true);
        }
        if (essenIndex >= 0 && essenIndex < shopEssenGefroren.size()) {
            shopEssenGefroren.set(essenIndex, true);
        }
    }

    /**
     * Kauft ein Essen und verwendet es auf einem Freund im Team.
     * Dies könnte z. B. Schaden und Leben erhöhen.
     * Die Kosten für Essen sind nicht definiert, nehmen wir an Kosten = 3 Gold.
     * @param essenIndex Index des Essens im Shop
     * @param teamIndex Index des Tieres im Team
     */
    public void buyItem(int essenIndex, int teamIndex) {
        if (essenIndex < 0 || essenIndex >= shopEssen.size()) {
            System.out.println("Ungültiger Essens-Index.");
            return;
        }
        if (teamIndex < 0 || teamIndex >= team.size()) {
            System.out.println("Ungültiger Tier-Index im Team.");
            return;
        }
        if (gold < BUY_FRIEND_COST) { // hier z.B. gleiche Kosten wie Tier kaufen
            System.out.println("Nicht genug Gold, um das Essen zu kaufen!");
            return;
        }
        
        Essen chosenEssen = shopEssen.get(essenIndex);
        if (chosenEssen == null) {
            System.out.println("Kein Essen in diesem Slot.");
            return;
        }

        Friends target = team.get(teamIndex);
        // Wende Effekte an
        target.setLeben(target.getLeben() + chosenEssen.getLebensEffekt());
        target.setSchaden(target.getSchaden() + chosenEssen.getSchadensEffekt());
        // Effekte mit beschwoerenEffekt (wenn vorhanden) könnten zusätzliche Logik erfordern

        gold -= BUY_FRIEND_COST;
        // Entferne das Essen aus dem Shop
        shopEssen.set(essenIndex, null);
    }

    public int getGold() {
        return gold;
    }

    public int getLeben() {
        return leben;
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
        this.shopTiere = shopTiere;
        this.shopTiereGefroren.clear();
        for (int i = 0; i < shopTiere.size(); i++) {
            shopTiereGefroren.add(false);
        }
    }

    public void setShopEssen(List<Essen> shopEssen) {
        this.shopEssen = shopEssen;
        this.shopEssenGefroren.clear();
        for (int i = 0; i < shopEssen.size(); i++) {
            shopEssenGefroren.add(false);
        }
    }

    /**
     * Hilfsmethode zum Generieren zufälliger Tiere für das Shop-Angebot.
     */
    private Friends generateRandomFriend() {
        // Hier könnte man aus einem Pool von Tieren zufällig wählen
        // Beispiel:
        String[] namen = {"Ameise", "Fisch", "Biber"};
        Random rand = new Random();
        String name = namen[rand.nextInt(namen.length)];
        int leben = rand.nextInt(3) + 1;
        int schaden = rand.nextInt(3) + 1;
        String effekt = "Kein Effekt";
        return new GenericFriend(name, leben, schaden, effekt);
    }

    /**
     * Hilfsmethode zum Generieren zufälligen Essens für das Shop-Angebot.
     */
    private Essen generateRandomEssen() {
        Random rand = new Random();
        int lebensEffekt = rand.nextInt(2) + 1; // +1 oder +2 Leben
        int schadensEffekt = rand.nextInt(2) + 1; // +1 oder +2 Schaden
        String beschwoeren = ""; // kein spezieller Effekt
        return new BasicEssen(lebensEffekt, schadensEffekt, beschwoeren);
    }
}
