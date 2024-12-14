public abstract class Essen {
    int lebensEffekt;
    int schadensEffekt;
    String beschwoerenEffekt;
    
    public Essen(int lebensEffekt, int schadensEffekt, String beschwoerenEffekt) {
        this.lebensEffekt = lebensEffekt;
        this.schadensEffekt = schadensEffekt;
        this.beschwoerenEffekt = beschwoerenEffekt;
    }

    public int getLebensEffekt() {
        return lebensEffekt;
    }

    public void setLebensEffekt(int lebensEffekt) {
        this.lebensEffekt = lebensEffekt;
    }

    public int getSchadensEffekt() {
        return schadensEffekt;
    }

    public void setSchadensEffekt(int schadensEffekt) {
        this.schadensEffekt = schadensEffekt;
    }

    public String getBeschwoerenEffekt() {
        return beschwoerenEffekt;
    }

    public void setBeschwoerenEffekt(String beschwoerenEffekt) {
        this.beschwoerenEffekt = beschwoerenEffekt;
    }
}
