package com.example.autohaendler.controller;

import com.example.autohaendler.DBConnection;
import com.example.autohaendler.model.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;


public class LoginController {

    @FXML private TextField txtBenutzername;
    @FXML private PasswordField txtPasswort;
    @FXML private Label lblFehler;
    @FXML private Button btnLogin;

    @FXML
    private void handleLogin() {
        String user = txtBenutzername.getText();
        String pass = txtPasswort.getText();

        String rolle = DBConnection.login(user, pass);

        if (rolle != null) {
            Session.rolle = rolle;
            Session.benutzername = user;
            // Hauptfenster öffnen
            openHauptfenster();
        } else {
            lblFehler.setText("Falsche Zugangsdaten!");
        }
    }
    private void openHauptfenster() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/autohaendler/view/hauptfenster.fxml"));
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
