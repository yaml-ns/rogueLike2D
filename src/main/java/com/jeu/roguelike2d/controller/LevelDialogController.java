package com.jeu.roguelike2d.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LevelDialogController {

    @FXML private VBox container;
    @FXML private Label title;
    @FXML private Label text;
    private GameController controller; // Référence au GameController

    @FXML
    public void initialize() {

    }

    public void updateUI() {
        if (controller.hasWin()) { // Utilisation directe de hasWin()
            title.setText("Félicitations !");
            text.setText("Vous avez gagné.");
        } else {
            title.setText("Loooooser !");
            text.setText("Vous avez perdu.");
        }
    }

    @FXML
    private void exitGame() {
        controller.exitGame();
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }
}