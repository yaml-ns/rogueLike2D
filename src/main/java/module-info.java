module com.jeu.roguelike2d {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens com.jeu.roguelike2d to javafx.fxml;
    exports com.jeu.roguelike2d;
    exports com.jeu.roguelike2d.controllers;
    opens com.jeu.roguelike2d.controllers to javafx.fxml;
}