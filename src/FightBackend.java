import java.util.ArrayList;
import java.util.List;
/**
 * Backend für das Kampf-Menü (Fight-Phase).
 * Hier wird ein 5-gegen-5 Kampf simuliert.
 * Das vorderste Tier jedes Teams tritt gegeneinander an und es wird so lange gekämpft,
 * bis ein Team keine Tiere mehr hat.
 */
public class FightBackend {
    private List<Friends> teamA;
    private List<Friends> teamB;

    public FightBackend(List<Friends> teamA, List<Friends> teamB) {
        this.teamA = new ArrayList<>(teamA);
        this.teamB = new ArrayList<>(teamB);
    }

    /**
     * Startet die Kampfphase. Simuliert den Kampf zwischen zwei Teams.
     * Gibt am Ende den Ausgang des Kampfes zurück: "A gewinnt", "B gewinnt" oder "Unentschieden"
     */
    public String startFight() {
        // Es wird angenommen, dass beide Teams bis zu 5 Tiere haben
        while (!teamA.isEmpty() && !teamB.isEmpty()) {
            Friends frontA = teamA.get(0);
            Friends frontB = teamB.get(0);

            // Beide Tiere fügen sich gegenseitig Schaden zu
            frontA.setLeben(frontA.getLeben() - frontB.getSchaden());
            frontB.setLeben(frontB.getLeben() - frontA.getSchaden());

            // Prüfe ob Tiere gestorben sind
            if (frontA.getLeben() <= 0 && frontB.getLeben() <= 0) {
                // Beide sterben
                teamA.remove(0);
                teamB.remove(0);
            } else if (frontA.getLeben() <= 0) {
                // Tier A stirbt
                teamA.remove(0);
            } else if (frontB.getLeben() <= 0) {
                // Tier B stirbt
                teamB.remove(0);
            }
            // Falls beide überleben, geht es in die nächste Runde.
        }

        if (teamA.isEmpty() && teamB.isEmpty()) {
            return "Unentschieden";
        } else if (teamA.isEmpty()) {
            return "B gewinnt";
        } else {
            return "A gewinnt";
        }
    }
}


/**
 * Ein generisches Friend-Objekt, um ein konkretes Tier zu erstellen.
 */
class GenericFriend extends Friends {
    public GenericFriend(String name, int leben, int schaden, String effekt) {
        super(name, leben, schaden, effekt);
    }
}

/**
 * Ein einfaches Essen-Objekt.
 */
class BasicEssen extends Essen {
    public BasicEssen(int lebensEffekt, int schadensEffekt, String beschwoerenEffekt) {
        super(lebensEffekt, schadensEffekt, beschwoerenEffekt);
    }
}
