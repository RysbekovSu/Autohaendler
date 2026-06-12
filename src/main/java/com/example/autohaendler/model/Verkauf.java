package com.example.autohaendler.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Verkauf {
    private int verkaufsNr;
    private int fahrzeug;
    private int kunde;
    private int mitarbeiter;
    private LocalDate datum;
    private BigDecimal preis;

    public Verkauf(int verkaufsNr, int fahrzeug, int kunde, int mitarbeiter,
                   LocalDate datum, BigDecimal preis) {
        this.verkaufsNr = verkaufsNr;
        this.fahrzeug = fahrzeug;
        this.kunde = kunde;
        this.mitarbeiter = mitarbeiter;
        this.datum = datum;
        this.preis = preis;
    }

    public int getVerkaufsNr()      { return verkaufsNr; }
    public int getFahrzeug()        { return fahrzeug; }
    public int getKunde()           { return kunde; }
    public int getMitarbeiter()     { return mitarbeiter; }
    public LocalDate getDatum()     { return datum; }
    public BigDecimal getPreis()    { return preis; }

    public void setVerkaufsNr(int v)       { this.verkaufsNr = v; }
    public void setFahrzeug(int v)         { this.fahrzeug = v; }
    public void setKunde(int v)            { this.kunde = v; }
    public void setMitarbeiter(int v)      { this.mitarbeiter = v; }
    public void setDatum(LocalDate v)      { this.datum = v; }
    public void setPreis(BigDecimal v)     { this.preis = v; }
}
