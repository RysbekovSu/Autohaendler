package com.example.autohaendler;

import java.sql.*;

public class DBConnection {

    private static final String URL =
            "jdbc:sqlserver://SULA\\SQLEXPRESS;"
                    + "databaseName=Autohandler2;"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;";

    private static final String USER = "autouser";
    private static final String PASS = "1234";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
    public static String login(String benutzername, String passwort) {
        String sql = "SELECT Rolle FROM Benutzer WHERE Benutzername = ? AND Passwort = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, benutzername);
            ps.setString(2, passwort);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("Rolle");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

