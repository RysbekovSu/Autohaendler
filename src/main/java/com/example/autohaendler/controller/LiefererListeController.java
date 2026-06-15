package com.example.autohaendler.controller;

import com.example.autohaendler.DBConnection;
import com.example.autohaendler.XmlExporter;
import com.example.autohaendler.model.Betrieb;
import com.example.autohaendler.model.Lieferer;
import com.example.autohaendler.model.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Pair;

import java.sql.*;

public class LiefererListeController {

    @FXML private TableView<Pair<Integer, Integer>> tabelle;
    @FXML private TableColumn<Pair<Integer, Integer>, String> colBetrieb;
    @FXML private TableColumn<Pair<Integer, Integer>, String> colLieferer;

    @FXML private ComboBox<Betrieb>  cbBetrieb;
    @FXML private ComboBox<Lieferer> cbLieferer;
    @FXML private ComboBox<Betrieb>  cbFilterBetrieb;
    @FXML private ComboBox<Lieferer> cbFilterLieferer;

    @FXML private Button btnHinzufuegen;
    @FXML private Button btnLoeschen;
    @FXML private Label  lblStatus;
    @FXML private Label  lblAnzahl;

    private final ObservableList<Pair<Integer, Integer>> daten    = FXCollections.observableArrayList();
    private final ObservableList<Betrieb>                betriebe = FXCollections.observableArrayList();
    private final ObservableList<Lieferer>               lieferer = FXCollections.observableArrayList();
    private FilteredList<Pair<Integer, Integer>>         gefilterteDaten;

    @FXML
    public void initialize() {
        colBetrieb.setCellValueFactory(cell -> {
            int id = cell.getValue().getKey();
            return new SimpleStringProperty(betriebe.stream()
                    .filter(b -> b.getBetriebsNr() == id)
                    .map(Betrieb::toString).findFirst().orElse(String.valueOf(id)));
        });
        colLieferer.setCellValueFactory(cell -> {
            int id = cell.getValue().getValue();
            return new SimpleStringProperty(lieferer.stream()
                    .filter(l -> l.getLiefererNr() == id)
                    .map(Lieferer::toString).findFirst().orElse(String.valueOf(id)));
        });

        gefilterteDaten = new FilteredList<>(daten, p -> true);
        tabelle.setItems(gefilterteDaten);
        gefilterteDaten.predicateProperty().addListener((o, a, n) ->
                lblAnzahl.setText("Einträge: " + gefilterteDaten.size()));

        cbBetrieb.setItems(betriebe);
        cbLieferer.setItems(lieferer);

        ObservableList<Betrieb> filterBetriebe = FXCollections.observableArrayList();
        filterBetriebe.add(null);
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

        ObservableList<Lieferer> filterLieferer = FXCollections.observableArrayList();
        filterLieferer.add(null);
        cbFilterLieferer.setItems(filterLieferer);
        cbFilterLieferer.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Lieferer l, boolean empty) {
                super.updateItem(l, empty);
                setText(l == null ? "Alle Lieferer" : l.toString());
            }
        });
        cbFilterLieferer.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Lieferer l, boolean empty) {
                super.updateItem(l, empty);
                setText(empty ? "" : (l == null ? "Alle Lieferer" : l.toString()));
            }
        });

        cbFilterBetrieb.valueProperty().addListener((o, a, n) -> aktualisiereFilter());
        cbFilterLieferer.valueProperty().addListener((o, a, n) -> aktualisiereFilter());

        tabelle.getSelectionModel().selectedItemProperty().addListener((obs, alt, neu) -> {
            if (neu != null) {
                betriebe.stream().filter(b -> b.getBetriebsNr() == neu.getKey())
                        .findFirst().ifPresent(cbBetrieb::setValue);
                lieferer.stream().filter(l -> l.getLiefererNr() == neu.getValue())
                        .findFirst().ifPresent(cbLieferer::setValue);
            }
        });

        boolean nurLesen = "Reader".equals(Session.rolle);
        btnHinzufuegen.setDisable(nurLesen);
        btnLoeschen.setDisable(!"Admin".equals(Session.rolle));

        ladeBetriebe();
        ladeLieferer();
        ladeDaten();

        filterBetriebe.addAll(betriebe);
        filterLieferer.addAll(lieferer);
        cbFilterBetrieb.setValue(null);
        cbFilterLieferer.setValue(null);
    }

    private void aktualisiereFilter() {
        Betrieb  fb = cbFilterBetrieb.getValue();
        Lieferer fl = cbFilterLieferer.getValue();
        gefilterteDaten.setPredicate(pair -> {
            boolean b = (fb == null) || pair.getKey()   == fb.getBetriebsNr();
            boolean l = (fl == null) || pair.getValue() == fl.getLiefererNr();
            return b && l;
        });
        lblAnzahl.setText("Einträge: " + gefilterteDaten.size());
    }

    @FXML private void handleFilterZuruecksetzen() {
        cbFilterBetrieb.setValue(null);
        cbFilterLieferer.setValue(null);
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

    private void ladeLieferer() {
        lieferer.clear();
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Lieferer ORDER BY LiefererNr")) {
            while (rs.next())
                lieferer.add(new Lieferer(rs.getInt("LiefererNr"), rs.getString("Firma"),
                        rs.getString("PLZ"), rs.getString("Strasse"), rs.getString("Hausnummer")));
        } catch (SQLException e) { lblStatus.setText("Fehler Lieferer: " + e.getMessage()); }
    }

    private void ladeDaten() {
        daten.clear();
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Betrieb, Lieferer FROM LiefererListe ORDER BY Betrieb, Lieferer")) {
            while (rs.next())
                daten.add(new Pair<>(rs.getInt("Betrieb"), rs.getInt("Lieferer")));
        } catch (SQLException e) { lblStatus.setText("Fehler: " + e.getMessage()); }
        lblAnzahl.setText("Einträge: " + daten.size());
    }

    private void leeren() {
        cbBetrieb.setValue(null);
        cbLieferer.setValue(null);
        tabelle.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleHinzufuegen() {
        if (cbBetrieb.getValue() == null || cbLieferer.getValue() == null) {
            lblStatus.setText("Bitte Betrieb und Lieferer auswählen!"); return;
        }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO LiefererListe (Betrieb, Lieferer) VALUES (?, ?)")) {
            ps.setInt(1, cbBetrieb.getValue().getBetriebsNr());
            ps.setInt(2, cbLieferer.getValue().getLiefererNr());
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
                             "DELETE FROM LiefererListe WHERE Betrieb = ? AND Lieferer = ?")) {
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
    @FXML private void handleXmlExport() { XmlExporter.exportiere("LiefererListe", tabelle);
    }}