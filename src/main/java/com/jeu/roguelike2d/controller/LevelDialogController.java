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
    private GameController controller;
    private boolean win = true;

    @FXML
    public void initialize(){
        if (win){
            title.setText("Felicitations !");
            text.setText("Vous avez gagné.");
        }else{
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


    public void setWin(boolean win) {
        this.win = win;
    }
}