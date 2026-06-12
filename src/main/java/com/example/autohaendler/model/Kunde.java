package com.example.autohaendler.model;

public class Kunde {
    private int kundenNr;
    private String nachname;
    private String vorname;
    private String plz;
    private String strasse;
    private String hausnummer;

    public Kunde(int kundenNr, String nachname, String vorname, String plz, String strasse, String hausnummer) {
        this.kundenNr = kundenNr;
        this.nachname = nachname;
        this.vorname = vorname;
        this.plz = plz;
        this.strasse = strasse;
        this.hausnummer = hausnummer;
    }

    public int getKundenNr()       { return kundenNr; }
    public String getNachname()    { return nachname; }
    public String getVorname()     { return vorname; }
    public String getPlz()         { return plz; }
    public String getStrasse()     { return strasse; }
    public String getHausnummer()  { return hausnummer; }

    public void setKundenNr(int v)      { this.kundenNr = v; }
    public void setNachname(String v)   { this.nachname = v; }
    public void setVorname(String v)    { this.vorname = v; }
    public void setPlz(String v)        { this.plz = v; }
    public void setStrasse(String v)    { this.strasse = v; }
    public void setHausnummer(String v) { this.hausnummer = v; }

    @Override public String toString() { return kundenNr + " – " + nachname + ", " + vorname; }
}
