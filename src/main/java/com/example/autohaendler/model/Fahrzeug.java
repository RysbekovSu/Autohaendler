package com.example.autohaendler.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Fahrzeug {
    private int fahrzeugNr;
    private String marke;
    private String modell;
    private BigDecimal kaufpreis;
    private BigDecimal listenpreis;
    private LocalDate herstelldatum;
    private int statusNr;

    public Fahrzeug(int fahrzeugNr, String marke, String modell,
                    BigDecimal kaufpreis, BigDecimal listenpreis,
                    LocalDate herstelldatum, int statusNr) {
        this.fahrzeugNr = fahrzeugNr;
        this.marke = marke;
        this.modell = modell;
        this.kaufpreis = kaufpreis;
        this.listenpreis = listenpreis;
        this.herstelldatum = herstelldatum;
        this.statusNr = statusNr;
    }

    public int getFahrzeugNr()          { return fahrzeugNr; }
    public String getMarke()            { return marke; }
    public String getModell()           { return modell; }
    public BigDecimal getKaufpreis()    { return kaufpreis; }
    public BigDecimal getListenpreis()  { return listenpreis; }
    public LocalDate getHerstelldatum() { return herstelldatum; }
    public int getStatusNr()            { return statusNr; }

    public void setFahrzeugNr(int v)           { this.fahrzeugNr = v; }
    public void setMarke(String v)             { this.marke = v; }
    public void setModell(String v)            { this.modell = v; }
    public void setKaufpreis(BigDecimal v)     { this.kaufpreis = v; }
    public void setListenpreis(BigDecimal v)   { this.listenpreis = v; }
    public void setHerstelldatum(LocalDate v)  { this.herstelldatum = v; }
    public void setStatusNr(int v)             { this.statusNr = v; }

    @Override public String toString() { return fahrzeugNr + " – " + marke + " " + modell; }
}
