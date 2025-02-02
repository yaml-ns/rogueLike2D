module com.jeu.roguelike2d {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires javafx.media;

    opens com.jeu.roguelike2d to javafx.fxml;
    exports com.jeu.roguelike2d;
    exports com.jeu.roguelike2d.controller;
    opens com.jeu.roguelike2d.controller to javafx.fxml;
    exports com.jeu.roguelike2d.model;
    opens com.jeu.roguelike2d.model to javafx.fxml;
}