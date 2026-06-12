module com.example.autohaendler {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;

    opens com.example.autohaendler to javafx.fxml;
    opens com.example.autohaendler.controller to javafx.fxml;
    opens com.example.autohaendler.model to javafx.base, javafx.fxml;

    exports com.example.autohaendler;
}
