package com.example.autohaendler;

import javafx.scene.Node;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.sql.*;

public class XmlExporter {


    public static void exportiere(String tabellenname, Node anyNode) {
        FileChooser fc = new FileChooser();
        fc.setTitle("XML-Export speichern");
        fc.setInitialFileName(tabellenname.toLowerCase() + "_export.xml");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML-Dateien", "*.xml"));
        File file = fc.showSaveDialog(anyNode.getScene().getWindow());
        if (file == null) return;

        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tabellenname);
             PrintWriter pw = new PrintWriter(file, "UTF-8")) {

            ResultSetMetaData meta = rs.getMetaData();
            int spalten = meta.getColumnCount();

            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<" + tabellenname + ">");

            while (rs.next()) {
                pw.println("  <Datensatz>");
                for (int i = 1; i <= spalten; i++) {
                    String spalte = meta.getColumnName(i);
                    String wert   = rs.getString(i);
                    pw.println("    <" + spalte + ">" + esc(wert) + "</" + spalte + ">");
                }
                pw.println("  </Datensatz>");
            }

            pw.println("</" + tabellenname + ">");

        } catch (Exception e) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "XML-Export fehlgeschlagen:\n" + e.getMessage());
            alert.showAndWait();
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
