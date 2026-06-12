package com.example.autohaendler.model;

public class Betrieb {
    private int betriebsNr;
    private String plz;
    private String strasse;
    private String hausnummer;

    public Betrieb(int betriebsNr, String plz, String strasse, String hausnummer) {
        this.betriebsNr = betriebsNr;
        this.plz = plz;
        this.strasse = strasse;
        this.hausnummer = hausnummer;
    }

    public int getBetriebsNr()     { return betriebsNr; }
    public String getPlz()         { return plz; }
    public String getStrasse()     { return strasse; }
    public String getHausnummer()  { return hausnummer; }

    public void setBetriebsNr(int v)     { this.betriebsNr = v; }
    public void setPlz(String v)         { this.plz = v; }
    public void setStrasse(String v)     { this.strasse = v; }
    public void setHausnummer(String v)  { this.hausnummer = v; }

    @Override public String toString() { return betriebsNr + " – " + strasse + " " + hausnummer + ", " + plz; }
}
