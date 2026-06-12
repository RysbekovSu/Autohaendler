package com.example.autohaendler.controller;

import com.example.autohaendler.DBConnection;
import com.example.autohaendler.model.Lieferer;
import com.example.autohaendler.model.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;

public class LiefererController {

    @FXML private TableView<Lieferer> tabelle;
    @FXML private TableColumn<Lieferer, Integer> colNr;
    @FXML private TableColumn<Lieferer, String>  colFirma;
    @FXML private TableColumn<Lieferer, String>  colPlz;
    @FXML private TableColumn<Lieferer, String>  colStrasse;
    @FXML private TableColumn<Lieferer, String>  colHausnummer;

    @FXML private TextField txtNr;
    @FXML private TextField txtFirma;
    @FXML private TextField txtPlz;
    @FXML private TextField txtStrasse;
    @FXML private TextField txtHausnummer;

    @FXML private Button btnHinzufuegen;
    @FXML private Button btnAendern;
    @FXML private Button btnLoeschen;
    @FXML private Label  lblStatus;

    private final ObservableList<Lieferer> daten = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNr.setCellValueFactory(new PropertyValueFactory<>("liefererNr"));
        colFirma.setCellValueFactory(new PropertyValueFactory<>("firma"));
        colPlz.setCellValueFactory(new PropertyValueFactory<>("plz"));
        colStrasse.setCellValueFactory(new PropertyValueFactory<>("strasse"));
        colHausnummer.setCellValueFactory(new PropertyValueFactory<>("hausnummer"));
        tabelle.setItems(daten);
        tabelle.getSelectionModel().selectedItemProperty().addListener((obs, alt, neu) -> {
            if (neu != null) {
                txtNr.setText(String.valueOf(neu.getLiefererNr()));
                txtFirma.setText(neu.getFirma());
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
             ResultSet rs = stmt.executeQuery("SELECT * FROM Lieferer ORDER BY LiefererNr")) {
            while (rs.next())
                daten.add(new Lieferer(rs.getInt("LiefererNr"), rs.getString("Firma"),
                        rs.getString("PLZ"), rs.getString("Strasse"), rs.getString("Hausnummer")));
        } catch (SQLException e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    private void leeren() {
        txtNr.clear(); txtFirma.clear(); txtPlz.clear(); txtStrasse.clear(); txtHausnummer.clear();
        tabelle.getSelectionModel().clearSelection();
    }

    @FXML private void handleHinzufuegen() {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO Lieferer VALUES (?,?,?,?,?)")) {
            ps.setInt(1, Integer.parseInt(txtNr.getText().trim()));
            ps.setString(2, txtFirma.getText().trim());
            ps.setString(3, txtPlz.getText().trim());
            ps.setString(4, txtStrasse.getText().trim());
            ps.setString(5, txtHausnummer.getText().trim());
            ps.executeUpdate();
            lblStatus.setText("Lieferer hinzugefuegt."); leeren(); ladeDaten();
        } catch (Exception e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    @FXML private void handleAendern() {
        Lieferer sel = tabelle.getSelectionModel().getSelectedItem();
        if (sel == null) { lblStatus.setText("Bitte auswaehlen!"); return; }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE Lieferer SET Firma=?,PLZ=?,Strasse=?,Hausnummer=? WHERE LiefererNr=?")) {
            ps.setString(1, txtFirma.getText().trim());
            ps.setString(2, txtPlz.getText().trim());
            ps.setString(3, txtStrasse.getText().trim());
            ps.setString(4, txtHausnummer.getText().trim());
            ps.setInt(5, sel.getLiefererNr());
            ps.executeUpdate();
            lblStatus.setText("Lieferer aktualisiert."); leeren(); ladeDaten();
        } catch (SQLException e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    @FXML private void handleLoeschen() {
        Lieferer sel = tabelle.getSelectionModel().getSelectedItem();
        if (sel == null) { lblStatus.setText("Bitte auswaehlen!"); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Wirklich loeschen?", ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement("DELETE FROM Lieferer WHERE LiefererNr=?")) {
                    ps.setInt(1, sel.getLiefererNr());
                    ps.executeUpdate();
                    lblStatus.setText("Geloescht."); leeren(); ladeDaten();
                } catch (SQLException e) { lblStatus.setText("Fehler: " + e.getMessage()); }
            }
        });
    }

    @FXML private void handleLeeren() { leeren(); lblStatus.setText(""); }
}
