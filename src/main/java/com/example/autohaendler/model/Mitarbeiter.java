package com.example.autohaendler.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Mitarbeiter {
    private int mitarbeiterNr;
    private String nachname;
    private String vorname;
    private int betriebsNr;
    private BigDecimal gehalt;
    private LocalDate einstellungsdatum;

    public Mitarbeiter(int mitarbeiterNr, String nachname, String vorname,
                       int betriebsNr, BigDecimal gehalt, LocalDate einstellungsdatum) {
        this.mitarbeiterNr = mitarbeiterNr;
        this.nachname = nachname;
        this.vorname = vorname;
        this.betriebsNr = betriebsNr;
        this.gehalt = gehalt;
        this.einstellungsdatum = einstellungsdatum;
    }

    public int getMitarbeiterNr()          { return mitarbeiterNr; }
    public String getNachname()            { return nachname; }
    public String getVorname()             { return vorname; }
    public int getBetriebsNr()             { return betriebsNr; }
    public BigDecimal getGehalt()          { return gehalt; }
    public LocalDate getEinstellungsdatum(){ return einstellungsdatum; }

    public void setMitarbeiterNr(int v)           { this.mitarbeiterNr = v; }
    public void setNachname(String v)             { this.nachname = v; }
    public void setVorname(String v)              { this.vorname = v; }
    public void setBetriebsNr(int v)              { this.betriebsNr = v; }
    public void setGehalt(BigDecimal v)           { this.gehalt = v; }
    public void setEinstellungsdatum(LocalDate v) { this.einstellungsdatum = v; }

    @Override public String toString() { return mitarbeiterNr + " – " + nachname + ", " + vorname; }
}
