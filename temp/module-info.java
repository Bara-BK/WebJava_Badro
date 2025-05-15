module Badro {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires java.desktop;
    requires com.google.gson;
    
    // Export packages
    exports tn.badro.entities;
    exports tn.badro.Controllers;
    exports tn.badro.services;
    exports tn.badro.tests;
    exports tn.badro.tools;
    
    // Open packages for reflection
    opens tn.badro.Controllers to javafx.fxml;
    opens tn.badro.tests to javafx.fxml;
    opens tn.badro.entities to javafx.fxml;
    opens tn.badro.services to java.logging, com.google.gson;
} 