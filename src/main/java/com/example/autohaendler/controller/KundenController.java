package com.example.autohaendler.controller;

import com.example.autohaendler.DBConnection;
import com.example.autohaendler.XmlExporter;
import com.example.autohaendler.model.Kunde;
import com.example.autohaendler.model.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

public class KundenController {

    @FXML private TableView<Kunde> tabelle;
    @FXML private TableColumn<Kunde, Integer> colNr;
    @FXML private TableColumn<Kunde, String>  colNachname;
    @FXML private TableColumn<Kunde, String>  colVorname;
    @FXML private TableColumn<Kunde, String>  colPlz;
    @FXML private TableColumn<Kunde, String>  colStrasse;
    @FXML private TableColumn<Kunde, String>  colHausnummer;

    @FXML private TextField txtNr;
    @FXML private TextField txtNachname;
    @FXML private TextField txtVorname;
    @FXML private TextField txtPlz;
    @FXML private TextField txtStrasse;
    @FXML private TextField txtHausnummer;

    @FXML private Button btnHinzufuegen;
    @FXML private Button btnAendern;
    @FXML private Button btnLoeschen;
    @FXML private Label  lblStatus;

    private final ObservableList<Kunde> daten = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNr.setCellValueFactory(new PropertyValueFactory<>("kundenNr"));
        colNachname.setCellValueFactory(new PropertyValueFactory<>("nachname"));
        colVorname.setCellValueFactory(new PropertyValueFactory<>("vorname"));
        colPlz.setCellValueFactory(new PropertyValueFactory<>("plz"));
        colStrasse.setCellValueFactory(new PropertyValueFactory<>("strasse"));
        colHausnummer.setCellValueFactory(new PropertyValueFactory<>("hausnummer"));

        tabelle.setItems(daten);

        tabelle.getSelectionModel().selectedItemProperty().addListener((obs, alt, neu) -> {
            if (neu != null) zeigeInFelder(neu);
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
             ResultSet rs = stmt.executeQuery("SELECT * FROM Kunden ORDER BY KundenNr")) {
            while (rs.next()) {
                daten.add(new Kunde(
                        rs.getInt("KundenNr"),
                        rs.getString("Nachname"),
                        rs.getString("Vorname"),
                        rs.getString("PLZ"),
                        rs.getString("Strasse"),
                        rs.getString("Hausnummer")
                ));
            }
        } catch (SQLException e) {
            lblStatus.setText("Fehler beim Laden: " + e.getMessage());
        }
    }

    private void zeigeInFelder(Kunde k) {
        txtNr.setText(String.valueOf(k.getKundenNr()));
        txtNachname.setText(k.getNachname());
        txtVorname.setText(k.getVorname());
        txtPlz.setText(k.getPlz());
        txtStrasse.setText(k.getStrasse());
        txtHausnummer.setText(k.getHausnummer());
    }

    private void felderLeeren() {
        txtNr.clear(); txtNachname.clear(); txtVorname.clear();
        txtPlz.clear(); txtStrasse.clear(); txtHausnummer.clear();
        tabelle.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleHinzufuegen() {
        if (txtNr.getText().isBlank() || txtNachname.getText().isBlank()) {
            lblStatus.setText("Nr und Nachname sind Pflichtfelder!"); return;
        }
        String sql = "INSERT INTO Kunden VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtNr.getText().trim()));
            ps.setString(2, txtNachname.getText().trim());
            ps.setString(3, txtVorname.getText().trim());
            ps.setString(4, txtPlz.getText().trim());
            ps.setString(5, txtStrasse.getText().trim());
            ps.setString(6, txtHausnummer.getText().trim());
            ps.executeUpdate();
            lblStatus.setText("Kunde hinzugefuegt.");
            felderLeeren(); ladeDaten();
        } catch (SQLException e) {
            lblStatus.setText("Fehler: " + e.getMessage());
        }
    }

    @FXML
    private void handleAendern() {
        Kunde sel = tabelle.getSelectionModel().getSelectedItem();
        if (sel == null) { lblStatus.setText("Bitte einen Kunden auswaehlen!"); return; }
        String sql = "UPDATE Kunden SET Nachname=?, Vorname=?, PLZ=?, Strasse=?, Hausnummer=? WHERE KundenNr=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtNachname.getText().trim());
            ps.setString(2, txtVorname.getText().trim());
            ps.setString(3, txtPlz.getText().trim());
            ps.setString(4, txtStrasse.getText().trim());
            ps.setString(5, txtHausnummer.getText().trim());
            ps.setInt(6, sel.getKundenNr());
            ps.executeUpdate();
            lblStatus.setText("Kunde aktualisiert.");
            felderLeeren(); ladeDaten();
        } catch (SQLException e) {
            lblStatus.setText("Fehler: " + e.getMessage());
        }
    }

    @FXML
    private void handleLoeschen() {
        Kunde sel = tabelle.getSelectionModel().getSelectedItem();
        if (sel == null) { lblStatus.setText("Bitte einen Kunden auswaehlen!"); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Kunde " + sel.getNachname() + " wirklich loeschen?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement("DELETE FROM Kunden WHERE KundenNr=?")) {
                    ps.setInt(1, sel.getKundenNr());
                    ps.executeUpdate();
                    lblStatus.setText("Kunde geloescht.");
                    felderLeeren(); ladeDaten();
                } catch (SQLException e) {
                    lblStatus.setText("Fehler: " + e.getMessage());
                }
            }
        });
    }

    @FXML private void handleLeeren() { felderLeeren(); lblStatus.setText(""); }

    @FXML
    private void handleXmlExport() {
        XmlExporter.exportiere("Kunden", tabelle);
    }

}
