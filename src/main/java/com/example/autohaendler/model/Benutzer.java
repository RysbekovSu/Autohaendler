package com.example.autohaendler.model;

public class Benutzer {
    private int benutzerNr;
    private String benutzername;
    private String passwort;
    private String rolle;

    public Benutzer(int benutzerNr, String benutzername, String passwort, String rolle) {
        this.benutzerNr = benutzerNr;
        this.benutzername = benutzername;
        this.passwort = passwort;
        this.rolle = rolle;
    }

    public int getBenutzerNr()      { return benutzerNr; }
    public String getBenutzername() { return benutzername; }
    public String getPasswort()     { return passwort; }
    public String getRolle()        { return rolle; }

    public void setBenutzerNr(int v)       { this.benutzerNr = v; }
    public void setBenutzername(String v)  { this.benutzername = v; }
    public void setPasswort(String v)      { this.passwort = v; }
    public void setRolle(String v)         { this.rolle = v; }
}
