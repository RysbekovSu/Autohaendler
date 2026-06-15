package com.example.autohaendler.controller;

import com.example.autohaendler.DBConnection;
import com.example.autohaendler.XmlExporter;
import com.example.autohaendler.model.Betrieb;
import com.example.autohaendler.model.Kunde;
import com.example.autohaendler.model.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Pair;

import java.sql.*;

public class KundenListeController {

    @FXML private TableView<Pair<Integer, Integer>> tabelle;
    @FXML private TableColumn<Pair<Integer, Integer>, String> colBetrieb;
    @FXML private TableColumn<Pair<Integer, Integer>, String> colKunde;

    @FXML private ComboBox<Betrieb> cbBetrieb;
    @FXML private ComboBox<Kunde>   cbKunde;
    @FXML private ComboBox<Betrieb> cbFilterBetrieb;
    @FXML private ComboBox<Kunde>   cbFilterKunde;

    @FXML private Button btnHinzufuegen;
    @FXML private Button btnLoeschen;
    @FXML private Label  lblStatus;
    @FXML private Label  lblAnzahl;

    private final ObservableList<Pair<Integer, Integer>> daten    = FXCollections.observableArrayList();
    private final ObservableList<Betrieb>                betriebe = FXCollections.observableArrayList();
    private final ObservableList<Kunde>                  kunden   = FXCollections.observableArrayList();
    private FilteredList<Pair<Integer, Integer>>         gefilterteDaten;

    @FXML
    public void initialize() {
        colBetrieb.setCellValueFactory(cell -> {
            int id = cell.getValue().getKey();
            return new SimpleStringProperty(betriebe.stream()
                    .filter(b -> b.getBetriebsNr() == id)
                    .map(Betrieb::toString).findFirst().orElse(String.valueOf(id)));
        });
        colKunde.setCellValueFactory(cell -> {
            int id = cell.getValue().getValue();
            return new SimpleStringProperty(kunden.stream()
                    .filter(k -> k.getKundenNr() == id)
                    .map(Kunde::toString).findFirst().orElse(String.valueOf(id)));
        });

        gefilterteDaten = new FilteredList<>(daten, p -> true);
        tabelle.setItems(gefilterteDaten);
        gefilterteDaten.predicateProperty().addListener((o, a, n) ->
                lblAnzahl.setText("Einträge: " + gefilterteDaten.size()));

        cbBetrieb.setItems(betriebe);
        cbKunde.setItems(kunden);

        // Filter-ComboBoxen: "Alle" als null-Eintrag
        ObservableList<Betrieb> filterBetriebe = FXCollections.observableArrayList();
        filterBetriebe.add(null); // "Alle"
        cbFilterBetrieb.setItems(filterBetriebe);
        cbFilterBetrieb.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Betrieb b, boolean empty) {
                super.updateItem(b, empty);
                setText(b == null ? "Alle Betriebe" : b.toString());
            }
        });
        cbFilterBetrieb.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Betrieb b, boolean empty) {
                super.updateItem(b, empty);
                setText(empty ? "" : (b == null ? "Alle Betriebe" : b.toString()));
            }
        });

        ObservableList<Kunde> filterKunden = FXCollections.observableArrayList();
        filterKunden.add(null);
        cbFilterKunde.setItems(filterKunden);
        cbFilterKunde.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Kunde k, boolean empty) {
                super.updateItem(k, empty);
                setText(k == null ? "Alle Kunden" : k.toString());
            }
        });
        cbFilterKunde.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Kunde k, boolean empty) {
                super.updateItem(k, empty);
                setText(empty ? "" : (k == null ? "Alle Kunden" : k.toString()));
            }
        });

        cbFilterBetrieb.valueProperty().addListener((o, a, n) -> aktualisiereFilter());
        cbFilterKunde.valueProperty().addListener((o, a, n) -> aktualisiereFilter());

        tabelle.getSelectionModel().selectedItemProperty().addListener((obs, alt, neu) -> {
            if (neu != null) {
                betriebe.stream().filter(b -> b.getBetriebsNr() == neu.getKey())
                        .findFirst().ifPresent(cbBetrieb::setValue);
                kunden.stream().filter(k -> k.getKundenNr() == neu.getValue())
                        .findFirst().ifPresent(cbKunde::setValue);
            }
        });

        boolean nurLesen = "Reader".equals(Session.rolle);
        btnHinzufuegen.setDisable(nurLesen);
        btnLoeschen.setDisable(!"Admin".equals(Session.rolle));

        ladeBetriebe();
        ladeKunden();
        ladeDaten();

        // Filter-Listen befüllen nach Laden
        filterBetriebe.addAll(betriebe);
        filterKunden.addAll(kunden);
        cbFilterBetrieb.setValue(null);
        cbFilterKunde.setValue(null);
    }

    private void aktualisiereFilter() {
        Betrieb fb = cbFilterBetrieb.getValue();
        Kunde   fk = cbFilterKunde.getValue();
        gefilterteDaten.setPredicate(pair -> {
            boolean b = (fb == null) || pair.getKey()   == fb.getBetriebsNr();
            boolean k = (fk == null) || pair.getValue() == fk.getKundenNr();
            return b && k;
        });
        lblAnzahl.setText("Einträge: " + gefilterteDaten.size());
    }

    @FXML private void handleFilterZuruecksetzen() {
        cbFilterBetrieb.setValue(null);
        cbFilterKunde.setValue(null);
    }

    private void ladeBetriebe() {
        betriebe.clear();
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Betriebe ORDER BY BetriebsNr")) {
            while (rs.next())
                betriebe.add(new Betrieb(rs.getInt("BetriebsNr"), rs.getString("PLZ"),
                        rs.getString("Strasse"), rs.getString("Hausnummer")));
        } catch (SQLException e) { lblStatus.setText("Fehler Betriebe: " + e.getMessage()); }
    }

    private void ladeKunden() {
        kunden.clear();
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Kunden ORDER BY KundenNr")) {
            while (rs.next())
                kunden.add(new Kunde(rs.getInt("KundenNr"), rs.getString("Nachname"),
                        rs.getString("Vorname"), rs.getString("PLZ"),
                        rs.getString("Strasse"), rs.getString("Hausnummer")));
        } catch (SQLException e) { lblStatus.setText("Fehler Kunden: " + e.getMessage()); }
    }

    private void ladeDaten() {
        daten.clear();
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Betrieb, Kunde FROM KundenListe ORDER BY Betrieb, Kunde")) {
            while (rs.next())
                daten.add(new Pair<>(rs.getInt("Betrieb"), rs.getInt("Kunde")));
        } catch (SQLException e) { lblStatus.setText("Fehler: " + e.getMessage()); }
        lblAnzahl.setText("Einträge: " + daten.size());
    }

    private void leeren() {
        cbBetrieb.setValue(null);
        cbKunde.setValue(null);
        tabelle.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleHinzufuegen() {
        if (cbBetrieb.getValue() == null || cbKunde.getValue() == null) {
            lblStatus.setText("Bitte Betrieb und Kunde auswählen!"); return;
        }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO KundenListe (Betrieb, Kunde) VALUES (?, ?)")) {
            ps.setInt(1, cbBetrieb.getValue().getBetriebsNr());
            ps.setInt(2, cbKunde.getValue().getKundenNr());
            ps.executeUpdate();
            lblStatus.setText("Eintrag hinzugefügt.");
            leeren(); ladeDaten();
        } catch (Exception e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    @FXML
    private void handleLoeschen() {
        Pair<Integer, Integer> sel = tabelle.getSelectionModel().getSelectedItem();
        if (sel == null) { lblStatus.setText("Bitte einen Eintrag auswählen!"); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Eintrag wirklich löschen?",
                ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(
                             "DELETE FROM KundenListe WHERE Betrieb = ? AND Kunde = ?")) {
                    ps.setInt(1, sel.getKey());
                    ps.setInt(2, sel.getValue());
                    ps.executeUpdate();
                    lblStatus.setText("Gelöscht.");
                    leeren(); ladeDaten();
                } catch (SQLException e) { lblStatus.setText("Fehler: " + e.getMessage()); }
            }
        });
    }

    @FXML private void handleLeeren() { leeren(); lblStatus.setText(""); }
    @FXML private void handleXmlExport() { XmlExporter.exportiere("KundenListe", tabelle); }
}
