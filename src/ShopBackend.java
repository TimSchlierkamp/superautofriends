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
        shopTiere.set(index, null);
    }

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
        gold += 1;
        team.remove(index);
    }

    public void freezeItem(int tierIndex, int essenIndex) {
        if (tierIndex >= 0 && tierIndex < shopTiereGefroren.size()) {
            shopTiereGefroren.set(tierIndex, true);
        }
        if (essenIndex >= 0 && essenIndex < shopEssenGefroren.size()) {
            shopEssenGefroren.set(essenIndex, true);
        }
    }

    public void buyItem(int essenIndex, int teamIndex) {
        if (essenIndex < 0 || essenIndex >= shopEssen.size()) {
            System.out.println("Ungültiger Essens-Index.");
            return;
        }
        if (teamIndex < 0 || teamIndex >= team.size()) {
            System.out.println("Ungültiger Tier-Index im Team.");
            return;
        }
        if (gold < BUY_FRIEND_COST) {
            System.out.println("Nicht genug Gold, um das Essen zu kaufen!");
            return;
        }

        Essen chosenEssen = shopEssen.get(essenIndex);
        if (chosenEssen == null) {
            System.out.println("Kein Essen in diesem Slot.");
            return;
        }

        Friends target = team.get(teamIndex);
        target.setLeben(target.getLeben() + chosenEssen.getLebensEffekt());
        target.setSchaden(target.getSchaden() + chosenEssen.getSchadensEffekt());

        gold -= BUY_FRIEND_COST;
        shopEssen.set(essenIndex, null);
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
    }

    public void setShopEssen(List<Essen> shopEssen) {
        this.shopEssen = new ArrayList<>(shopEssen);
        this.shopEssenGefroren.clear();
        for (int i = 0; i < shopEssen.size(); i++) {
            shopEssenGefroren.add(false);
        }
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
}
