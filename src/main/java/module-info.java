module bikram.bikram_sas {
    // Core JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Database + Utility
    requires java.sql;
    requires com.sun.jna;

    // Security
    requires de.mkammerer.argon2;
    requires de.mkammerer.argon2.nolibs;

    // Logging, PDF, etc.
    requires org.slf4j;
    requires static lombok;
    requires java.management;
    requires org.xerial.sqlitejdbc;
    requires java.scripting;
    requires org.apache.pdfbox;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires java.desktop;
    requires javafx.swing;
    requires javafx.graphics;
    requires com.google.gson;
    requires webcam.capture;
    requires bridj;
    requires java.rmi;
    requires jdk.jfr;
    requires java.net.http;
    requires librealsense;


    // ✅ (already correct for JavaFX / ORM)
    opens bikram.model to javafx.base;
    opens bikram.views.page to javafx.fxml;
    opens bikram.views.ui to javafx.fxml;
    opens bikram.security to de.mkammerer.argon2;

    // ✅ Exported packages
    exports bikram;
    exports bikram.model;
    exports bikram.views.page;
    exports bikram.views.ui;
    exports bikram.security;
}
