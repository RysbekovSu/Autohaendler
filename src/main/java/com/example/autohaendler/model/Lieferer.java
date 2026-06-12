package com.example.autohaendler.model;

public class Lieferer {
    private int liefererNr;
    private String firma;
    private String plz;
    private String strasse;
    private String hausnummer;

    public Lieferer(int liefererNr, String firma, String plz, String strasse, String hausnummer) {
        this.liefererNr = liefererNr;
        this.firma = firma;
        this.plz = plz;
        this.strasse = strasse;
        this.hausnummer = hausnummer;
    }

    public int getLiefererNr()     { return liefererNr; }
    public String getFirma()       { return firma; }
    public String getPlz()         { return plz; }
    public String getStrasse()     { return strasse; }
    public String getHausnummer()  { return hausnummer; }

    public void setLiefererNr(int v)     { this.liefererNr = v; }
    public void setFirma(String v)       { this.firma = v; }
    public void setPlz(String v)         { this.plz = v; }
    public void setStrasse(String v)     { this.strasse = v; }
    public void setHausnummer(String v)  { this.hausnummer = v; }

    @Override public String toString() { return liefererNr + " – " + firma; }
}
