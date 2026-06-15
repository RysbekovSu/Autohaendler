package com.example.autohaendler.controller;

import com.example.autohaendler.model.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class HauptfensterController {

    @FXML private TabPane tabPane;
    @FXML private Label   lblBenutzer;

    // Reihenfolge: Betriebe VOR Mitarbeiter
    private static final String[] TAB_NAMEN = {
            "Kunden","Kundenliste", "Betriebe", "Mitarbeiter", "Lieferer","Liefererliste", "Fahrzeuge", "Verkauf", "Benutzer"

    };
    private static final String[] FXML_DATEIEN = {
            "kunden.fxml","kundenliste.fxml", "betriebe.fxml", "mitarbeiter.fxml", "lieferer.fxml", "liefererliste.fxml",
            "fahrzeuge.fxml", "verkauf.fxml", "benutzer.fxml",

    };

    @FXML
    public void initialize() {
        lblBenutzer.setText("👤 " + Session.benutzername + "  |  " + Session.rolle);

        for (int i = 0; i < tabPane.getTabs().size() && i < TAB_NAMEN.length; i++) {
            Tab tab = tabPane.getTabs().get(i);
            tab.setText(TAB_NAMEN[i]);
            final int index = i;
            tab.setOnSelectionChanged(e -> {
                if (tab.isSelected() && tab.getContent() == null) {
                    ladeView(tab, FXML_DATEIEN[index]);
                }
            });
        }

        if (!"Admin".equals(Session.rolle)) {
            tabPane.getTabs().removeIf(t -> "Benutzer".equals(t.getText()));
        }

        if (!tabPane.getTabs().isEmpty()) {
            ladeView(tabPane.getTabs().get(0), FXML_DATEIEN[0]);
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Session.rolle = null;
            Session.benutzername = null;
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/autohaendler/view/login.fxml"));
            Stage stage = (Stage) tabPane.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 600, 400));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ladeView(Tab tab, String fxmlDatei) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/autohaendler/view/tabelle/" + fxmlDatei));
            Node view = loader.load();
            AnchorPane wrapper = new AnchorPane(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
            tab.setContent(wrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
