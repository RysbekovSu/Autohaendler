package com.example.autohaendler.model;

public class Status {
    private int statusNr;
    private String beschreibung;

    public Status(int statusNr, String beschreibung) {
        this.statusNr = statusNr;
        this.beschreibung = beschreibung;
    }

    public int getStatusNr()          { return statusNr; }
    public String getBeschreibung()   { return beschreibung; }

    public void setStatusNr(int v)         { this.statusNr = v; }
    public void setBeschreibung(String v)  { this.beschreibung = v; }

    @Override public String toString() { return statusNr + " – " + beschreibung; }
}
