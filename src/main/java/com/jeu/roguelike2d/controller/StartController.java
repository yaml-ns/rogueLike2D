package com.jeu.roguelike2d.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class StartController {
    @FXML private TextField nameField;
    @FXML private Button startButton;



    @FXML
    public void startGame() throws IOException {
        String playerName = nameField.getText().trim();
        if (!playerName.isEmpty()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/GameView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) startButton.getScene().getWindow();
            Scene gameScene = new Scene(root, 1920, 1080);
            stage.setScene(gameScene);
        }
    }
}
