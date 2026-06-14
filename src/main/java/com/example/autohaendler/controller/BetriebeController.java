package com.example.autohaendler.controller;

import com.example.autohaendler.DBConnection;
import com.example.autohaendler.model.Betrieb;
import com.example.autohaendler.model.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.sql.*;

public class BetriebeController {

    @FXML private TableView<Betrieb> tabelle;
    @FXML private TableColumn<Betrieb, Integer> colNr;
    @FXML private TableColumn<Betrieb, String>  colPlz;
    @FXML private TableColumn<Betrieb, String>  colStrasse;
    @FXML private TableColumn<Betrieb, String>  colHausnummer;

    @FXML private TextField txtNr;
    @FXML private TextField txtPlz;
    @FXML private TextField txtStrasse;
    @FXML private TextField txtHausnummer;

    @FXML private Button btnHinzufuegen;
    @FXML private Button btnAendern;
    @FXML private Button btnLoeschen;
    @FXML private Label  lblStatus;

    private final ObservableList<Betrieb> daten = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNr.setCellValueFactory(new PropertyValueFactory<>("betriebsNr"));
        colPlz.setCellValueFactory(new PropertyValueFactory<>("plz"));
        colStrasse.setCellValueFactory(new PropertyValueFactory<>("strasse"));
        colHausnummer.setCellValueFactory(new PropertyValueFactory<>("hausnummer"));

        tabelle.setItems(daten);

        tabelle.getSelectionModel().selectedItemProperty().addListener((obs, alt, neu) -> {
            if (neu != null) {
                txtNr.setText(String.valueOf(neu.getBetriebsNr()));
                txtPlz.setText(neu.getPlz());
                txtStrasse.setText(neu.getStrasse());
                txtHausnummer.setText(neu.getHausnummer());
            }
        });

        boolean nurLesen = "Reader".equals(Session.rolle);
        btnHinzufuegen.setDisable(nurLesen);
        btnAendern.setDisable(nurLesen);
        btnLoeschen.setDisable(!"Admin".equals(Session.rolle));

        ladeDaten();
    }

    private void ladeDaten() {
        daten.clear();
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Betriebe ORDER BY BetriebsNr")) {
            while (rs.next())
                daten.add(new Betrieb(
                        rs.getInt("BetriebsNr"), rs.getString("PLZ"),
                        rs.getString("Strasse"), rs.getString("Hausnummer")));
        } catch (SQLException e) {
            lblStatus.setText("Fehler: " + e.getMessage());
        }
    }

    private void leeren() {
        txtNr.clear(); txtPlz.clear(); txtStrasse.clear(); txtHausnummer.clear();
        tabelle.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleHinzufuegen() {
        if (txtNr.getText().isBlank()) { lblStatus.setText("Nr ist Pflichtfeld!"); return; }
        String sql = "INSERT INTO Betriebe VALUES (?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtNr.getText().trim()));
            ps.setString(2, txtPlz.getText().trim());
            ps.setString(3, txtStrasse.getText().trim());
            ps.setString(4, txtHausnummer.getText().trim());
            ps.executeUpdate();
            lblStatus.setText("Betrieb hinzugefuegt."); leeren(); ladeDaten();
        } catch (Exception e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    @FXML
    private void handleAendern() {
        Betrieb sel = tabelle.getSelectionModel().getSelectedItem();
        if (sel == null) { lblStatus.setText("Bitte auswaehlen!"); return; }
        String sql = "UPDATE Betriebe SET PLZ=?,Strasse=?,Hausnummer=? WHERE BetriebsNr=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtPlz.getText().trim());
            ps.setString(2, txtStrasse.getText().trim());
            ps.setString(3, txtHausnummer.getText().trim());
            ps.setInt(4, sel.getBetriebsNr());
            ps.executeUpdate();
            lblStatus.setText("Aktualisiert."); leeren(); ladeDaten();
        } catch (Exception e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    @FXML
    private void handleLoeschen() {
        Betrieb sel = tabelle.getSelectionModel().getSelectedItem();
        if (sel == null) { lblStatus.setText("Bitte auswaehlen!"); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Betrieb " + sel.getBetriebsNr() + " wirklich loeschen?", ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement("DELETE FROM Betriebe WHERE BetriebsNr=?")) {
                    ps.setInt(1, sel.getBetriebsNr());
                    ps.executeUpdate();
                    lblStatus.setText("Geloescht."); leeren(); ladeDaten();
                } catch (SQLException e) { lblStatus.setText("Fehler: " + e.getMessage()); }
            }
        });
    }

    @FXML private void handleLeeren() { leeren(); lblStatus.setText(""); }

    // ── XML-Export ────────────────────────────────────────────────────────────
    @FXML
    private void handleXmlExport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("XML-Export speichern");
        fc.setInitialFileName("betriebe_export.xml");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML-Dateien", "*.xml"));
        File file = fc.showSaveDialog(tabelle.getScene().getWindow());
        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(file, "UTF-8")) {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<Betriebe>");
            for (Betrieb b : daten) {
                pw.println("  <Betrieb>");
                pw.println("    <BetriebsNr>" + b.getBetriebsNr() + "</BetriebsNr>");
                pw.println("    <PLZ>" + esc(b.getPlz()) + "</PLZ>");
                pw.println("    <Strasse>" + esc(b.getStrasse()) + "</Strasse>");
                pw.println("    <Hausnummer>" + esc(b.getHausnummer()) + "</Hausnummer>");
                pw.println("  </Betrieb>");
            }
            pw.println("</Betriebe>");
            lblStatus.setText("XML exportiert nach: " + file.getName());
        } catch (Exception e) {
            lblStatus.setText("Export-Fehler: " + e.getMessage());
        }
    }

    /** Sonderzeichen fuer XML escapen */
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
