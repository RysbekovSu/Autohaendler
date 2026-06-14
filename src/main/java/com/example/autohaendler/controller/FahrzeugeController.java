package com.example.autohaendler.controller;

import com.example.autohaendler.DBConnection;
import com.example.autohaendler.XmlExporter;
import com.example.autohaendler.model.Fahrzeug;
import com.example.autohaendler.model.Session;
import com.example.autohaendler.model.Status;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

public class FahrzeugeController {

    @FXML private TableView<Fahrzeug> tabelle;
    @FXML private TableColumn<Fahrzeug, Integer>    colNr;
    @FXML private TableColumn<Fahrzeug, String>     colMarke;
    @FXML private TableColumn<Fahrzeug, String>     colModell;
    @FXML private TableColumn<Fahrzeug, BigDecimal> colKaufpreis;
    @FXML private TableColumn<Fahrzeug, BigDecimal> colListenpreis;
    @FXML private TableColumn<Fahrzeug, LocalDate>  colHerstelldatum;
    @FXML private TableColumn<Fahrzeug, Integer>    colStatus;

    @FXML private TextField       txtNr;
    @FXML private TextField       txtMarke;
    @FXML private TextField       txtModell;
    @FXML private TextField       txtKaufpreis;
    @FXML private TextField       txtListenpreis;
    @FXML private DatePicker      dpHerstelldatum;
    @FXML private ComboBox<Status> cbStatus;   // Dropdown

    @FXML private Button btnHinzufuegen;
    @FXML private Button btnAendern;
    @FXML private Button btnLoeschen;
    @FXML private Label  lblStatus;

    private final ObservableList<Fahrzeug> daten    = FXCollections.observableArrayList();
    private final ObservableList<Status>   statusListe = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNr.setCellValueFactory(new PropertyValueFactory<>("fahrzeugNr"));
        colMarke.setCellValueFactory(new PropertyValueFactory<>("marke"));
        colModell.setCellValueFactory(new PropertyValueFactory<>("modell"));
        colKaufpreis.setCellValueFactory(new PropertyValueFactory<>("kaufpreis"));
        colListenpreis.setCellValueFactory(new PropertyValueFactory<>("listenpreis"));
        colHerstelldatum.setCellValueFactory(new PropertyValueFactory<>("herstelldatum"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusNr"));

        tabelle.setItems(daten);
        cbStatus.setItems(statusListe);

        tabelle.getSelectionModel().selectedItemProperty().addListener((obs, alt, neu) -> {
            if (neu != null) {
                txtNr.setText(String.valueOf(neu.getFahrzeugNr()));
                txtMarke.setText(neu.getMarke());
                txtModell.setText(neu.getModell());
                txtKaufpreis.setText(neu.getKaufpreis().toPlainString());
                txtListenpreis.setText(neu.getListenpreis().toPlainString());
                dpHerstelldatum.setValue(neu.getHerstelldatum());
                statusListe.stream().filter(s -> s.getStatusNr() == neu.getStatusNr())
                        .findFirst().ifPresent(cbStatus::setValue);
            }
        });

        boolean nurLesen = "Reader".equals(Session.rolle);
        btnHinzufuegen.setDisable(nurLesen);
        btnAendern.setDisable(nurLesen);
        btnLoeschen.setDisable(!"Admin".equals(Session.rolle));

        ladeStatus();
        ladeDaten();
    }

    private void ladeStatus() {
        statusListe.clear();
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Status ORDER BY StatusNr")) {
            while (rs.next())
                statusListe.add(new Status(rs.getInt("StatusNr"), rs.getString("Beschreibung")));
        } catch (SQLException e) { lblStatus.setText("Status-Fehler: " + e.getMessage()); }
    }

    private void ladeDaten() {
        daten.clear();
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Fahrzeuge ORDER BY FahrzeugNr")) {
            while (rs.next())
                daten.add(new Fahrzeug(
                        rs.getInt("FahrzeugNr"), rs.getString("Marke"), rs.getString("Modell"),
                        rs.getBigDecimal("Kaufpreis"), rs.getBigDecimal("Listenpreis"),
                        rs.getDate("Herstelldatum").toLocalDate(), rs.getInt("StatusNr")));
        } catch (SQLException e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    private void leeren() {
        txtNr.clear(); txtMarke.clear(); txtModell.clear();
        txtKaufpreis.clear(); txtListenpreis.clear();
        dpHerstelldatum.setValue(null); cbStatus.setValue(null);
        tabelle.getSelectionModel().clearSelection();
    }

    @FXML private void handleHinzufuegen() {
        if (cbStatus.getValue() == null) { lblStatus.setText("Status auswaehlen!"); return; }
        String sql = "INSERT INTO Fahrzeuge VALUES (?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(txtNr.getText().trim()));
            ps.setString(2, txtMarke.getText().trim());
            ps.setString(3, txtModell.getText().trim());
            ps.setBigDecimal(4, new BigDecimal(txtKaufpreis.getText().trim()));
            ps.setBigDecimal(5, new BigDecimal(txtListenpreis.getText().trim()));
            ps.setDate(6, Date.valueOf(dpHerstelldatum.getValue()));
            ps.setInt(7, cbStatus.getValue().getStatusNr());
            ps.executeUpdate();
            lblStatus.setText("Fahrzeug hinzugefuegt."); leeren(); ladeDaten();
        } catch (Exception e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    @FXML private void handleAendern() {
        Fahrzeug sel = tabelle.getSelectionModel().getSelectedItem();
        if (sel == null || cbStatus.getValue() == null) { lblStatus.setText("Auswahl fehlt!"); return; }
        String sql = "UPDATE Fahrzeuge SET Marke=?,Modell=?,Kaufpreis=?,Listenpreis=?,Herstelldatum=?,StatusNr=? WHERE FahrzeugNr=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtMarke.getText().trim());
            ps.setString(2, txtModell.getText().trim());
            ps.setBigDecimal(3, new BigDecimal(txtKaufpreis.getText().trim()));
            ps.setBigDecimal(4, new BigDecimal(txtListenpreis.getText().trim()));
            ps.setDate(5, Date.valueOf(dpHerstelldatum.getValue()));
            ps.setInt(6, cbStatus.getValue().getStatusNr());
            ps.setInt(7, sel.getFahrzeugNr());
            ps.executeUpdate();
            lblStatus.setText("Aktualisiert."); leeren(); ladeDaten();
        } catch (Exception e) { lblStatus.setText("Fehler: " + e.getMessage()); }
    }

    @FXML private void handleLoeschen() {
        Fahrzeug sel = tabelle.getSelectionModel().getSelectedItem();
        if (sel == null) { lblStatus.setText("Bitte auswaehlen!"); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Wirklich loeschen?", ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement("DELETE FROM Fahrzeuge WHERE FahrzeugNr=?")) {
                    ps.setInt(1, sel.getFahrzeugNr());
                    ps.executeUpdate();
                    lblStatus.setText("Geloescht."); leeren(); ladeDaten();
                } catch (SQLException e) { lblStatus.setText("Fehler: " + e.getMessage()); }
            }
        });
    }

    @FXML private void handleLeeren() { leeren(); lblStatus.setText(""); }

    @FXML
    private void handleXmlExport() {
        XmlExporter.exportiere("Fahrzeuge", tabelle);
    }

}
