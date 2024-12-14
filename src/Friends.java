public abstract class Friends {
    protected String name;
    protected int leben;
    protected int schaden;
    protected String effekt;
    
    public Friends(String name, int leben, int schaden, String effekt) {
        this.name = name;
        this.leben = leben;
        this.schaden = schaden;
        this.effekt = effekt;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLeben() {
        return leben;
    }

    public void setLeben(int leben) {
        this.leben = leben;
    }

    public int getSchaden() {
        return schaden;
    }

    public void setSchaden(int schaden) {
        this.schaden = schaden;
    }

    public String getEffekt() {
        return effekt;
    }

    public void setEffekt(String effekt) {
        this.effekt = effekt;
    }



}
