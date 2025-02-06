package com.jeu.roguelike2d.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;
public class StartController {

    @FXML private ImageView backgroundImage;
    @FXML private TextField nameField;
    @FXML private Button startButton;
    @FXML private Button closeButton;

    @FXML
    public void initialize(){
        Image image = new Image(getClass().getResource("/com/jeu/roguelike2d/images/game-bg-2.png").toExternalForm());
        backgroundImage.setImage(image);
        double width = Screen.getPrimary().getBounds().getWidth();
        double height = Screen.getPrimary().getBounds().getHeight();

        backgroundImage.setFitWidth(width);
        backgroundImage.setFitHeight(height);
        backgroundImage.setPreserveRatio(false);



    }

    @FXML
    public void startGame() throws IOException {
        String playerName = nameField.getText().trim();
        if (!playerName.isEmpty()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jeu/roguelike2d/view/game-view.fxml"));
            Parent root = loader.load();
            double width = Screen.getPrimary().getBounds().getWidth();
            double height = Screen.getPrimary().getBounds().getHeight();
            GameController controller = loader.getController();
            controller.setName("Nom : " + playerName);
            Stage stage = (Stage) startButton.getScene().getWindow();
            Scene gameScene = new Scene(root, width, height);
            stage.setScene(gameScene);
        }
    }

    @FXML
    private void handleCloseButtonAction() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
