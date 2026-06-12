package com.example.autohaendler.controller;

import com.example.autohaendler.DBConnection;
import com.example.autohaendler.model.Benutzer;
import com.example.autohaendler.model.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;

public class BenutzerController {

    @FXML private TableView<Benutzer> tabelle;
    @FXML private TableColumn<Benutzer, Integer> colNr;
    @FXML private TableColumn<Benutzer, String>  colBenutzername;
    @FXML private TableColumn<Benutzer, String>  colPasswort;
    @FXML private TableColumn<Benutzer, String>  colRolle;

    @FXML private TextField       txtNr;
    @FXML private TextField       txtBenutzername;
    @FXML private PasswordField   txtPasswort;
    @FXML private ComboBox<String> cbRolle;   // Dropdown

    @FXML private Button btnHinzufuegen;
    @FXML private Button btnAendern;
    @FXML private Button btnLoeschen;
    @FXML private Label  lblStatus;

    private final ObservableList<Benutzer> daten = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNr.setCellValueFactory(new PropertyValueFactory<>("benutzerNr"));
        colBenutzername.setCellValueFactory(new PropertyValueFactory<>("benutzername"));
        colPasswort.setCellValueFactory(new PropertyValueFactory<>("passwort"));
        colRolle.setCellValueFactory(new PropertyValueFactory<>("rolle"));

        tabelle.setItems(daten);
        cbRolle.setItems(FXCollections.observableArrayList("Reader", "ReadWriter", "Admin"));

        tabelle.getSelectionModel().selectedItemProperty().addListener((obs, alt, neu) -> {
            if (neu != null) {
                txtNr.setText(String.valueOf(neu.getBenutzerNr()));
                txtBenutzername.setText(neu.getBenutzername());
                txtPasswort.setText(neu.getPasswort());
                cbRolle.setValue(neu.getRolle());
            }
        });

        // Nur Admin darf Benutzer verwalten
        boolean istAdmin = "Admin".equals(Session.rolle);
        btnHinzufuegen.setDisable(!istAdmin);
        btnAendern.setDisable(!istAdmin);
        btnLoeschen.setDisable(!istAdmin);

        ladeDaten();
    }

    private void ladeDaten() {
        daten.clear();
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Benutzer ORDER BY BenutzerNr")) {
            while (rs.next())
                daten.add(new Benutzer(rs.getInt("BenutzerNr"), rs.getString("Benutzername"),
                        rs.getString("Passwort"), rs.getString("Rolle")));
        } catch (SQLException e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    private void leeren() {
        txtNr.clear(); txtBenutzername.clear(); txtPasswort.clear(); cbRolle.setValue(null);
        tabelle.getSelectionModel().clearSelection();
    }

    @FXML private void handleHinzufuegen() {
        if (cbRolle.getValue() == null) { lblStatus.setText("Rolle auswaehlen!"); return; }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO Benutzer VALUES (?,?,?,?)")) {
            ps.setInt(1, Integer.parseInt(txtNr.getText().trim()));
            ps.setString(2, txtBenutzername.getText().trim());
            ps.setString(3, txtPasswort.getText().trim());
            ps.setString(4, cbRolle.getValue());
            ps.executeUpdate();
            lblStatus.setText("Benutzer hinzugefuegt."); leeren(); ladeDaten();
        } catch (Exception e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    @FXML private void handleAendern() {
        Benutzer sel = tabelle.getSelectionModel().getSelectedItem();
        if (sel == null) { lblStatus.setText("Bitte auswaehlen!"); return; }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE Benutzer SET Benutzername=?,Passwort=?,Rolle=? WHERE BenutzerNr=?")) {
            ps.setString(1, txtBenutzername.getText().trim());
            ps.setString(2, txtPasswort.getText().trim());
            ps.setString(3, cbRolle.getValue());
            ps.setInt(4, sel.getBenutzerNr());
            ps.executeUpdate();
            lblStatus.setText("Aktualisiert."); leeren(); ladeDaten();
        } catch (Exception e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    @FXML private void handleLoeschen() {
        Benutzer sel = tabelle.getSelectionModel().getSelectedItem();
        if (sel == null) { lblStatus.setText("Bitte auswaehlen!"); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Wirklich loeschen?", ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement("DELETE FROM Benutzer WHERE BenutzerNr=?")) {
                    ps.setInt(1, sel.getBenutzerNr()); ps.executeUpdate();
                    lblStatus.setText("Geloescht."); leeren(); ladeDaten();
                } catch (SQLException e) { lblStatus.setText("Fehler: " + e.getMessage()); }
            }
        });
    }

    @FXML private void handleLeeren() { leeren(); lblStatus.setText(""); }
}
