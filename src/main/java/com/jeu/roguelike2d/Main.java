package com.jeu.roguelike2d;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    private MediaPlayer epicSounds;
    @Override
    public void start(Stage primaryStage) throws Exception {
        double width = Screen.getPrimary().getBounds().getWidth();
        double height = Screen.getPrimary().getBounds().getHeight();
        FXMLLoader loader = new FXMLLoader(com.jeu.roguelike2d.Main.class.getResource("view/start-view.fxml"));
        Scene scene = new Scene(loader.load(), width, height);

        scene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setScene(scene);
        primaryStage.setX(0);
        primaryStage.setY(0);
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        primaryStage.setResizable(false);
        primaryStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        primaryStage.show();
        String epicPath = getClass().getResource("/com/jeu/roguelike2d/sons/epic.wav").toString();
        Media media = new Media(epicPath);
        epicSounds = new MediaPlayer(media);
        epicSounds.setCycleCount(MediaPlayer.INDEFINITE);
        epicSounds.setOnEndOfMedia(() -> epicSounds.seek(javafx.util.Duration.ZERO));
        epicSounds.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
