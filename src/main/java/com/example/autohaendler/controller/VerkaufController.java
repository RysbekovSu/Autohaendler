package com.example.autohaendler.controller;

import com.example.autohaendler.DBConnection;
import com.example.autohaendler.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

public class VerkaufController {

    @FXML private TableView<Verkauf> tabelle;
    @FXML private TableColumn<Verkauf, Integer>    colNr;
    @FXML private TableColumn<Verkauf, Integer>    colFahrzeug;
    @FXML private TableColumn<Verkauf, Integer>    colKunde;
    @FXML private TableColumn<Verkauf, Integer>    colMitarbeiter;
    @FXML private TableColumn<Verkauf, LocalDate>  colDatum;
    @FXML private TableColumn<Verkauf, BigDecimal> colPreis;

    @FXML private TextField          txtNr;
    @FXML private ComboBox<Fahrzeug>    cbFahrzeug;     // Dropdown
    @FXML private ComboBox<Kunde>       cbKunde;        // Dropdown
    @FXML private ComboBox<Mitarbeiter> cbMitarbeiter;  // Dropdown
    @FXML private DatePicker            dpDatum;
    @FXML private TextField             txtPreis;

    @FXML private Button btnHinzufuegen;
    @FXML private Button btnAendern;
    @FXML private Button btnLoeschen;
    @FXML private Label  lblStatus;

    private final ObservableList<Verkauf>     daten        = FXCollections.observableArrayList();
    private final ObservableList<Fahrzeug>    fahrzeuge    = FXCollections.observableArrayList();
    private final ObservableList<Kunde>       kunden       = FXCollections.observableArrayList();
    private final ObservableList<Mitarbeiter> mitarbeiter  = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNr.setCellValueFactory(new PropertyValueFactory<>("verkaufsNr"));
        colFahrzeug.setCellValueFactory(new PropertyValueFactory<>("fahrzeug"));
        colKunde.setCellValueFactory(new PropertyValueFactory<>("kunde"));
        colMitarbeiter.setCellValueFactory(new PropertyValueFactory<>("mitarbeiter"));
        colDatum.setCellValueFactory(new PropertyValueFactory<>("datum"));
        colPreis.setCellValueFactory(new PropertyValueFactory<>("preis"));

        tabelle.setItems(daten);
        cbFahrzeug.setItems(fahrzeuge);
        cbKunde.setItems(kunden);
        cbMitarbeiter.setItems(mitarbeiter);

        tabelle.getSelectionModel().selectedItemProperty().addListener((obs, alt, neu) -> {
            if (neu != null) {
                txtNr.setText(String.valueOf(neu.getVerkaufsNr()));
                fahrzeuge.stream().filter(f -> f.getFahrzeugNr() == neu.getFahrzeug()).findFirst().ifPresent(cbFahrzeug::setValue);
                kunden.stream().filter(k -> k.getKundenNr() == neu.getKunde()).findFirst().ifPresent(cbKunde::setValue);
                mitarbeiter.stream().filter(m -> m.getMitarbeiterNr() == neu.getMitarbeiter()).findFirst().ifPresent(cbMitarbeiter::setValue);
                dpDatum.setValue(neu.getDatum());
                txtPreis.setText(neu.getPreis().toPlainString());
            }
        });

        boolean nurLesen = "Reader".equals(Session.rolle);
        btnHinzufuegen.setDisable(nurLesen);
        btnAendern.setDisable(nurLesen);
        btnLoeschen.setDisable(!"Admin".equals(Session.rolle));

        ladeFahrzeuge(); ladeKunden(); ladeMitarbeiter(); ladeDaten();
    }

    private void ladeFahrzeuge() {
        fahrzeuge.clear();
        try (Connection con = DBConnection.getConnection(); Statement s = con.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM Fahrzeuge ORDER BY FahrzeugNr")) {
            while (rs.next()) fahrzeuge.add(new Fahrzeug(rs.getInt("FahrzeugNr"), rs.getString("Marke"),
                    rs.getString("Modell"), rs.getBigDecimal("Kaufpreis"), rs.getBigDecimal("Listenpreis"),
                    rs.getDate("Herstelldatum").toLocalDate(), rs.getInt("StatusNr")));
        } catch (SQLException e) { lblStatus.setText(e.getMessage()); }
    }

    private void ladeKunden() {
        kunden.clear();
        try (Connection con = DBConnection.getConnection(); Statement s = con.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM Kunden ORDER BY KundenNr")) {
            while (rs.next()) kunden.add(new Kunde(rs.getInt("KundenNr"), rs.getString("Nachname"),
                    rs.getString("Vorname"), rs.getString("PLZ"), rs.getString("Strasse"), rs.getString("Hausnummer")));
        } catch (SQLException e) { lblStatus.setText(e.getMessage()); }
    }

    private void ladeMitarbeiter() {
        mitarbeiter.clear();
        try (Connection con = DBConnection.getConnection(); Statement s = con.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM Mitarbeiter ORDER BY MitarbeiterNr")) {
            while (rs.next()) mitarbeiter.add(new Mitarbeiter(rs.getInt("MitarbeiterNr"), rs.getString("Nachname"),
                    rs.getString("Vorname"), rs.getInt("BetriebsNr"), rs.getBigDecimal("Gehalt"),
                    rs.getDate("Einstellungsdatum").toLocalDate()));
        } catch (SQLException e) { lblStatus.setText(e.getMessage()); }
    }

    private void ladeDaten() {
        daten.clear();
        try (Connection con = DBConnection.getConnection(); Statement s = con.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM Verkaeufe ORDER BY VerkaufsNr")) {
            while (rs.next()) daten.add(new Verkauf(rs.getInt("VerkaufsNr"), rs.getInt("Fahrzeug"),
                    rs.getInt("Kunde"), rs.getInt("Mitarbeiter"),
                    rs.getDate("Datum").toLocalDate(), rs.getBigDecimal("Preis")));
        } catch (SQLException e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    private void leeren() {
        txtNr.clear(); cbFahrzeug.setValue(null); cbKunde.setValue(null);
        cbMitarbeiter.setValue(null); dpDatum.setValue(null); txtPreis.clear();
        tabelle.getSelectionModel().clearSelection();
    }

    @FXML private void handleHinzufuegen() {
        if (cbFahrzeug.getValue()==null || cbKunde.getValue()==null || cbMitarbeiter.getValue()==null) {
            lblStatus.setText("Alle Dropdowns auswaehlen!"); return;
        }
        String sql = "INSERT INTO Verkaeufe VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtNr.getText().trim()));
            ps.setInt(2, cbFahrzeug.getValue().getFahrzeugNr());
            ps.setInt(3, cbKunde.getValue().getKundenNr());
            ps.setInt(4, cbMitarbeiter.getValue().getMitarbeiterNr());
            ps.setDate(5, Date.valueOf(dpDatum.getValue()));
            ps.setBigDecimal(6, new BigDecimal(txtPreis.getText().trim()));
            ps.executeUpdate();
            lblStatus.setText("Verkauf hinzugefuegt."); leeren(); ladeDaten();
        } catch (Exception e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    @FXML private void handleAendern() {
        Verkauf sel = tabelle.getSelectionModel().getSelectedItem();
        if (sel==null) { lblStatus.setText("Bitte auswaehlen!"); return; }
        String sql = "UPDATE Verkaeufe SET Fahrzeug=?,Kunde=?,Mitarbeiter=?,Datum=?,Preis=? WHERE VerkaufsNr=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cbFahrzeug.getValue().getFahrzeugNr());
            ps.setInt(2, cbKunde.getValue().getKundenNr());
            ps.setInt(3, cbMitarbeiter.getValue().getMitarbeiterNr());
            ps.setDate(4, Date.valueOf(dpDatum.getValue()));
            ps.setBigDecimal(5, new BigDecimal(txtPreis.getText().trim()));
            ps.setInt(6, sel.getVerkaufsNr());
            ps.executeUpdate();
            lblStatus.setText("Aktualisiert."); leeren(); ladeDaten();
        } catch (Exception e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    @FXML private void handleLoeschen() {
        Verkauf sel = tabelle.getSelectionModel().getSelectedItem();
        if (sel==null) { lblStatus.setText("Bitte auswaehlen!"); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Wirklich loeschen?", ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement("DELETE FROM Verkaeufe WHERE VerkaufsNr=?")) {
                    ps.setInt(1, sel.getVerkaufsNr()); ps.executeUpdate();
                    lblStatus.setText("Geloescht."); leeren(); ladeDaten();
                } catch (SQLException e) { lblStatus.setText("Fehler: " + e.getMessage()); }
            }
        });
    }

    @FXML private void handleLeeren() { leeren(); lblStatus.setText(""); }
}
