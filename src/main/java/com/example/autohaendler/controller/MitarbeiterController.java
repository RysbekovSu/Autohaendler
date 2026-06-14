package com.example.autohaendler.controller;

import com.example.autohaendler.DBConnection;
import com.example.autohaendler.XmlExporter;
import com.example.autohaendler.model.Betrieb;
import com.example.autohaendler.model.Mitarbeiter;
import com.example.autohaendler.model.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

public class MitarbeiterController {

    @FXML private TableView<Mitarbeiter> tabelle;
    @FXML private TableColumn<Mitarbeiter, Integer>    colNr;
    @FXML private TableColumn<Mitarbeiter, String>     colNachname;
    @FXML private TableColumn<Mitarbeiter, String>     colVorname;
    @FXML private TableColumn<Mitarbeiter, Integer>    colBetrieb;
    @FXML private TableColumn<Mitarbeiter, BigDecimal> colGehalt;
    @FXML private TableColumn<Mitarbeiter, LocalDate>  colDatum;

    @FXML private TextField   txtNr;
    @FXML private TextField   txtNachname;
    @FXML private TextField   txtVorname;
    @FXML private ComboBox<Betrieb> cbBetrieb;   // Dropdown
    @FXML private TextField   txtGehalt;
    @FXML private DatePicker  dpDatum;

    @FXML private Button btnHinzufuegen;
    @FXML private Button btnAendern;
    @FXML private Button btnLoeschen;
    @FXML private Label  lblStatus;

    private final ObservableList<Mitarbeiter> daten    = FXCollections.observableArrayList();
    private final ObservableList<Betrieb>     betriebe = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNr.setCellValueFactory(new PropertyValueFactory<>("mitarbeiterNr"));
        colNachname.setCellValueFactory(new PropertyValueFactory<>("nachname"));
        colVorname.setCellValueFactory(new PropertyValueFactory<>("vorname"));
        colBetrieb.setCellValueFactory(new PropertyValueFactory<>("betriebsNr"));
        colGehalt.setCellValueFactory(new PropertyValueFactory<>("gehalt"));
        colDatum.setCellValueFactory(new PropertyValueFactory<>("einstellungsdatum"));

        tabelle.setItems(daten);
        cbBetrieb.setItems(betriebe);

        tabelle.getSelectionModel().selectedItemProperty().addListener((obs, alt, neu) -> {
            if (neu != null) {
                txtNr.setText(String.valueOf(neu.getMitarbeiterNr()));
                txtNachname.setText(neu.getNachname());
                txtVorname.setText(neu.getVorname());
                betriebe.stream().filter(b -> b.getBetriebsNr() == neu.getBetriebsNr())
                        .findFirst().ifPresent(cbBetrieb::setValue);
                txtGehalt.setText(neu.getGehalt().toPlainString());
                dpDatum.setValue(neu.getEinstellungsdatum());
            }
        });

        boolean nurLesen = "Reader".equals(Session.rolle);
        btnHinzufuegen.setDisable(nurLesen);
        btnAendern.setDisable(nurLesen);
        btnLoeschen.setDisable(!"Admin".equals(Session.rolle));

        ladeBetriebe();
        ladeDaten();
    }

    private void ladeBetriebe() {
        betriebe.clear();
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Betriebe ORDER BY BetriebsNr")) {
            while (rs.next())
                betriebe.add(new Betrieb(rs.getInt("BetriebsNr"), rs.getString("PLZ"),
                        rs.getString("Strasse"), rs.getString("Hausnummer")));
        } catch (SQLException e) { lblStatus.setText("Betriebe-Fehler: " + e.getMessage()); }
    }

    private void ladeDaten() {
        daten.clear();
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Mitarbeiter ORDER BY MitarbeiterNr")) {
            while (rs.next())
                daten.add(new Mitarbeiter(
                        rs.getInt("MitarbeiterNr"), rs.getString("Nachname"), rs.getString("Vorname"),
                        rs.getInt("BetriebsNr"), rs.getBigDecimal("Gehalt"),
                        rs.getDate("Einstellungsdatum").toLocalDate()));
        } catch (SQLException e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    private void leeren() {
        txtNr.clear(); txtNachname.clear(); txtVorname.clear();
        cbBetrieb.setValue(null); txtGehalt.clear(); dpDatum.setValue(null);
        tabelle.getSelectionModel().clearSelection();
    }

    @FXML private void handleHinzufuegen() {
        if (cbBetrieb.getValue() == null) { lblStatus.setText("Betrieb auswaehlen!"); return; }
        String sql = "INSERT INTO Mitarbeiter VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtNr.getText().trim()));
            ps.setString(2, txtNachname.getText().trim());
            ps.setString(3, txtVorname.getText().trim());
            ps.setInt(4, cbBetrieb.getValue().getBetriebsNr());
            ps.setBigDecimal(5, new BigDecimal(txtGehalt.getText().trim()));
            ps.setDate(6, Date.valueOf(dpDatum.getValue()));
            ps.executeUpdate();
            lblStatus.setText("Mitarbeiter hinzugefuegt."); leeren(); ladeDaten();
        } catch (Exception e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    @FXML private void handleAendern() {
        Mitarbeiter sel = tabelle.getSelectionModel().getSelectedItem();
        if (sel == null || cbBetrieb.getValue() == null) { lblStatus.setText("Auswahl fehlt!"); return; }
        String sql = "UPDATE Mitarbeiter SET Nachname=?,Vorname=?,BetriebsNr=?,Gehalt=?,Einstellungsdatum=? WHERE MitarbeiterNr=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtNachname.getText().trim());
            ps.setString(2, txtVorname.getText().trim());
            ps.setInt(3, cbBetrieb.getValue().getBetriebsNr());
            ps.setBigDecimal(4, new BigDecimal(txtGehalt.getText().trim()));
            ps.setDate(5, Date.valueOf(dpDatum.getValue()));
            ps.setInt(6, sel.getMitarbeiterNr());
            ps.executeUpdate();
            lblStatus.setText("Aktualisiert."); leeren(); ladeDaten();
        } catch (Exception e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    @FXML private void handleLoeschen() {
        Mitarbeiter sel = tabelle.getSelectionModel().getSelectedItem();
        if (sel == null) { lblStatus.setText("Bitte auswaehlen!"); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Wirklich loeschen?", ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement("DELETE FROM Mitarbeiter WHERE MitarbeiterNr=?")) {
                    ps.setInt(1, sel.getMitarbeiterNr());
                    ps.executeUpdate();
                    lblStatus.setText("Geloescht."); leeren(); ladeDaten();
                } catch (SQLException e) { lblStatus.setText("Fehler: " + e.getMessage()); }
            }
        });
    }

    @FXML private void handleLeeren() { leeren(); lblStatus.setText(""); }

    @FXML
    private void handleXmlExport() {
        XmlExporter.exportiere("Mitarbeiter", tabelle);
    }

}
