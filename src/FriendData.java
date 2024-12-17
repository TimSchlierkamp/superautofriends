public class FriendData {
    private String name;
    private int leben;
    private int schaden;

    public FriendData(String name, int leben, int schaden) {
        this.name = name;
        this.leben = leben;
        this.schaden = schaden;
    }

    // Getter und Setter
    public String getName() {
        return name;
    }

    public int getLeben() {
        return leben;
    }

    public int getSchaden() {
        return schaden;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLeben(int leben) {
        this.leben = leben;
    }

    public void setSchaden(int schaden) {
        this.schaden = schaden;
    }
}
