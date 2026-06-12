package com.example.autohaendler.controller;

import com.example.autohaendler.model.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

public class HauptfensterController {

    @FXML private TabPane tabPane;

    private static final String[] TAB_NAMEN = {
        "Kunden", "Mitarbeiter", "Lieferer", "Fahrzeuge", "Verkauf", "Benutzer"
    };
    private static final String[] FXML_DATEIEN = {
        "kunden.fxml", "mitarbeiter.fxml", "lieferer.fxml",
        "fahrzeuge.fxml", "verkauf.fxml", "benutzer.fxml"
    };

    @FXML
    public void initialize() {
        // Benutzer-Tab nur für Admin sichtbar
        for (int i = 0; i < tabPane.getTabs().size(); i++) {
            Tab tab = tabPane.getTabs().get(i);
            tab.setText(TAB_NAMEN[i]);
            final int index = i;

            // Lazy loading: View erst laden wenn Tab ausgewählt wird
            tab.setOnSelectionChanged(e -> {
                if (tab.isSelected() && tab.getContent() == null) {
                    ladeView(tab, FXML_DATEIEN[index]);
                }
            });
        }

        // Benutzer-Tab verstecken wenn kein Admin
        if (!"Admin".equals(Session.rolle)) {
            tabPane.getTabs().removeIf(t -> "Benutzer".equals(t.getText()));
        }

        // Ersten Tab sofort laden
        if (!tabPane.getTabs().isEmpty()) {
            Tab ersterTab = tabPane.getTabs().get(0);
            ladeView(ersterTab, FXML_DATEIEN[0]);
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
