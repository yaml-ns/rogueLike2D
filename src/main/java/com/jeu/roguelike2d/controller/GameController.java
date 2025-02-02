package com.jeu.roguelike2d.controller;

import com.jeu.roguelike2d.model.Player;
import com.jeu.roguelike2d.utils.MazeGenerator;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
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
    @FXML private Button exit;
    @FXML private Pane mazeContainer;
    private MazeGenerator maze;
    private AnimationTimer timer;
    private Player player;
    private int cellSize = 60;

    public void initialize() {
        Image upTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-up.png")));
        Image downTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-front.png")));
        Image leftTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-left.png")));
        Image rightTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-right.png")));

        player = new Player(0, 0, upTexture, downTexture, leftTexture, rightTexture);

        topBar.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((o, oldVal, newVal) -> resizeUI(newScene));
                newScene.heightProperty().addListener((o, oldVal, newVal) -> resizeUI(newScene));
                newScene.setOnKeyPressed(event -> handlePlayerMovement(event.getCode()));
                generateMaze();
                Platform.runLater(() -> newScene.getRoot().requestFocus());
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

        maze = new MazeGenerator(cols, rows, cellSize,
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/briques.jpg"))),
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/pave.png"))),
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/porte.jpg"))));

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
        gc.drawImage(player.getCurrentTexture(), player.getX() * cellSize, player.getY() * cellSize, cellSize, cellSize);
    }

    private void handlePlayerMovement(KeyCode keyCode) {
        switch (keyCode) {
            case UP:
                if (maze.canMove(player.getX(), player.getY(), 0, -1)) {
                    player.setY(player.getY() - 1);
                    player.setDirection(Player.Direction.UP);
                }
                break;
            case DOWN:
                if (maze.canMove(player.getX(), player.getY(), 0, 1)) {
                    player.setY(player.getY() + 1);
                    player.setDirection(Player.Direction.DOWN);
                }
                break;
            case LEFT:
                if (maze.canMove(player.getX(), player.getY(), -1, 0)) {
                    player.setX(player.getX() - 1);
                    player.setDirection(Player.Direction.LEFT);
                }
                break;
            case RIGHT:
                if (maze.canMove(player.getX(), player.getY(), 1, 0)) {
                    player.setX(player.getX() + 1);
                    player.setDirection(Player.Direction.RIGHT);
                }
                break;
        }
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        maze.draw(gc);
        drawPlayer(gc);
        checkIfPlayerReachedDoor();
    }

    private void checkIfPlayerReachedDoor() {
        if (maze.getDoorCell() != null && player.getX() == maze.getDoorCell().getX() && player.getY() == maze.getDoorCell().getY()) {
            System.out.println("Player reached the door!");
            // Vous pouvez ajouter ici une action spécifique lorsque le joueur atteint la porte
        }
    }

    @FXML
    public void exitGame(){
        Stage stage = (Stage) exit.getScene().getWindow();
        stage.close();
    }
}
