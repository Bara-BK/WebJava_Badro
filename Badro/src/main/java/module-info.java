module Badro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires javafx.media;

    opens tn.badro.tests to javafx.fxml;
    opens tn.badro.Controllers to javafx.fxml;

    exports tn.badro.tests;
    exports tn.badro.entities;
    exports tn.badro.services;
    exports tn.badro.tools;
    exports tn.badro.Controllers;
}
