package com.jeu.roguelike2d.controller;

import com.jeu.roguelike2d.utils.MazeGenerator;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class GameController {
    @FXML private Canvas canvas;
    @FXML private HBox topBar;
    @FXML private VBox sideBar;
    @FXML private Pane mazeContainer;
    @FXML private Button exit;
    private MazeGenerator maze;
    private AnimationTimer timer;
    private Image wallTexture;
    private Image floorTexture;
    private Image playerTexture;
    private int playerX, playerY; // Position du joueur
    private int cellSize = 60;

    public void initialize() {
        wallTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/briques.jpg")));
        floorTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/pave.png")));
        playerTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/soldier.png")));

        topBar.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((o, oldVal, newVal) -> resizeUI(newScene));
                newScene.heightProperty().addListener((o, oldVal, newVal) -> resizeUI(newScene));
                newScene.setOnKeyPressed(event -> handlePlayerMovement(event.getCode()));
                generateMaze();
            }
        });
    }

    private void resizeUI(Scene scene) {
        canvas.setWidth(scene.getWidth());
        canvas.setHeight(scene.getHeight());
        generateMaze();
    }

    private void generateMaze() {
        if (canvas.getWidth() == 0 || canvas.getHeight() == 0) return;

        int cols = (int) (canvas.getWidth() / cellSize);
        int rows = (int) (canvas.getHeight() / cellSize);

        if (cols == 0 || rows == 0) return;

        maze = new MazeGenerator(cols, rows, cellSize, wallTexture, floorTexture);
        playerX = 0;
        playerY = 0;

        GraphicsContext gc = canvas.getGraphicsContext2D();

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                if (!maze.generateStep()) {
                    stop();
                }
                maze.draw(gc);
                drawPlayer(gc);
            }
        };
        timer.start();
    }

    private void drawPlayer(GraphicsContext gc) {
        gc.drawImage(playerTexture, playerX * cellSize, playerY * cellSize, cellSize, cellSize);
    }

    private void handlePlayerMovement(KeyCode keyCode) {
        System.out.println(keyCode);
        switch (keyCode) {
            case UP:
                if (maze.canMove(playerX, playerY, 0, -1)) {
                    playerY--;
                }
                break;
            case DOWN:
                if (maze.canMove(playerX, playerY, 0, 1)) {
                    playerY++;
                }
                break;
            case LEFT:
                if (maze.canMove(playerX, playerY, -1, 0)) {
                    playerX--;
                }
                break;
            case RIGHT:
                if (maze.canMove(playerX, playerY, 1, 0)) {
                    playerX++;
                }
                break;
        }
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        maze.draw(gc);
        drawPlayer(gc);
    }

    @FXML
    public void  exitGame(){
        Stage stage = (Stage) exit.getScene().getWindow();
        stage.close();
    }
}
