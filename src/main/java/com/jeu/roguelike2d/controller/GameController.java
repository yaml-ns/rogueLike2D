package com.jeu.roguelike2d.controller;

import com.jeu.roguelike2d.model.*;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameController {
    @FXML private Canvas canvas;
    @FXML private HBox topBar;
    @FXML private VBox sideBar;
    @FXML private Button exit;
    @FXML private Pane mazeContainer;

    private MazeGenerator maze;
    private Player player;

    private List<Monster> monsters = new ArrayList<>();
    private int currentDx = 0;
    private int currentDy = 0;
    private boolean isMoving = false;
    private AnimationTimer monsterMovementAnimation;
    @FXML
    public void initialize() {
        canvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                canvas.widthProperty().bind(newScene.widthProperty());
                canvas.heightProperty().bind(newScene.heightProperty());
                newScene.windowProperty().addListener((obs2, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        updateGrid();
                    }
                });

                canvas.setOnKeyPressed(event -> {
                    switch (event.getCode()) {
                        case UP -> { currentDx = 0; currentDy = -1; }
                        case DOWN -> { currentDx = 0; currentDy = 1; }
                        case LEFT -> { currentDx = -1; currentDy = 0; }
                        case RIGHT -> { currentDx = 1; currentDy = 0; }
                    }
                    startMovement();
                });


                canvas.setOnKeyReleased(event -> {
                    switch (event.getCode()) {
                        case UP, DOWN -> currentDy = 0;
                        case LEFT, RIGHT -> currentDx = 0;
                    }
                });
                canvas.setFocusTraversable(true);
                startMonsterMovement();
            }
        });

        mazeContainer.widthProperty().addListener((obs, oldVal, newVal) -> updateGrid());
        mazeContainer.heightProperty().addListener((obs, oldVal, newVal) -> updateGrid());
    }

    private void startMovement() {
        if (!isMoving && (currentDx != 0 || currentDy != 0)) {
            movePlayer(currentDx, currentDy);
        }
    }

    private void startMonsterMovement() {
        monsterMovementAnimation = new AnimationTimer() {
            @Override
            public void handle(long now) {
                for (Monster monster : monsters) {
                    if (monster.isAlive()) {
                        monster.autoMove(maze);
                    }
                }
                drawMaze();
            }
        };
        monsterMovementAnimation.start();
    }
    private void movePlayer(int dx, int dy) {
        if (player.move(dx, dy, maze)) {
            isMoving = true;
            animateEntityMovement(player, dx, dy, this::startMovement);
        } else {
            isMoving = false;
        }
    }

    private void animateEntityMovement(Entity entity, int dx, int dy, Runnable onComplete) {
        int cellWidth = (int) Math.round(canvas.getWidth() / maze.getCols());
        int cellHeight = (int) Math.round(canvas.getHeight() / maze.getRows());

        double targetX = entity.getX() * cellWidth;
        double targetY = entity.getY() * cellHeight;

        double startX = entity.getRealX();
        double startY = entity.getRealY();

        final double speed = 40;
        final long startTime = System.nanoTime();

        AnimationTimer movementAnimation = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double elapsedSeconds = (now - startTime) / 1_000_000_000.0;
                double progress = elapsedSeconds * speed;

                if (progress >= Math.hypot(targetX - startX, targetY - startY)) {

                    entity.setRealX(targetX);
                    entity.setRealY(targetY);
                    this.stop();
                    drawMaze();

                    isMoving = false;
                    onComplete.run();
                } else {
                    double newX = startX + (targetX - startX) * (progress / Math.hypot(targetX - startX, targetY - startY));
                    double newY = startY + (targetY - startY) * (progress / Math.hypot(targetX - startX, targetY - startY));
                    entity.setRealX(newX);
                    entity.setRealY(newY);
                    drawMaze();
                }
            }
        };

        movementAnimation.start();
    }

    public void animateMonsterMovement(Monster monster, int dx, int dy) {
        int cellWidth = (int) Math.round(canvas.getWidth() / maze.getCols());
        int cellHeight = (int) Math.round(canvas.getHeight() / maze.getRows());

        double targetX = monster.getX() * cellWidth; // Position cible en pixels
        double targetY = monster.getY() * cellHeight;

        double startX = monster.getRealX(); // Position actuelle en pixels
        double startY = monster.getRealY();

        final double speed = 300; // Pixels par seconde
        final long startTime = System.nanoTime();

        AnimationTimer movementAnimation = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double elapsedSeconds = (now - startTime) / 1_000_000_000.0;
                double progress = elapsedSeconds * speed;

                if (progress >= Math.hypot(targetX - startX, targetY - startY)) {
                    // Forcer l'alignement exact à la fin de l'animation
                    monster.setRealX(targetX);
                    monster.setRealY(targetY);
                    this.stop();
                    drawMaze(); // Redessiner après l'animation
                } else {
                    // Calculer la position intermédiaire
                    double newX = startX + (targetX - startX) * (progress / Math.hypot(targetX - startX, targetY - startY));
                    double newY = startY + (targetY - startY) * (progress / Math.hypot(targetX - startX, targetY - startY));
                    monster.setRealX(newX);
                    monster.setRealY(newY);
                    drawMaze(); // Redessiner pendant l'animation
                }
            }
        };

        movementAnimation.start();
    }
    private void drawEntity(Entity entity, GraphicsContext gc) {
        int cellWidth = (int) Math.round(canvas.getWidth() / maze.getCols());
        int cellHeight = (int) Math.round(canvas.getHeight() / maze.getRows());

        double px = entity.getRealX();
        double py = entity.getRealY();

        if (entity.getTexture() != null) {
            gc.drawImage(entity.getTexture(), px, py, cellWidth, cellHeight);
        } else {

            gc.setFill(Color.GRAY);
            gc.fillRect(px, py, cellWidth, cellHeight);
        }
    }
    /**
     * Met à jour les dimensions du labyrinthe et redessine
     */

    private void updateGrid() {

        if (canvas.getScene() == null) {
            return;
        }

        double screenWidth = canvas.getScene().getWidth();
        double screenHeight = canvas.getScene().getHeight();

        int cols = 35;
        int rows = 20;

        int cellWidth = (int) Math.round(screenWidth / cols);
        int cellHeight = (int) Math.round(screenHeight / rows);

        Image wallTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/briques.jpg")));
        Image floorTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/pave.png")));
        Image doorTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/porte.jpg")));

        maze = new MazeGenerator(cols, rows, cellWidth, cellHeight, wallTexture, floorTexture, doorTexture);
        while (maze.generateStep());
        player = new Player(100,0,"Djamel",new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-right.png"))));

        monsters.clear();


        for (int i = 0; i < 3; i++) {
            int[] monsterPosition = getRandomValidPosition();

            Image dragonTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/dragon.png")));
            Dragon dragon = new Dragon(monsterPosition[0], monsterPosition[1], dragonTexture);
            dragon.setRealX(monsterPosition[0] * cellWidth);
            dragon.setRealY(monsterPosition[1] * cellHeight);
            dragon.setController(this);
            monsters.add(dragon);
        }

        for (int i = 0; i < 5; i++) {
            int[] monsterPosition = getRandomValidPosition();

            Image chupacabraTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/chupacabra.png")));
            Chupacabra chupacabra = new Chupacabra(monsterPosition[0], monsterPosition[1], chupacabraTexture, player);
            chupacabra.setRealX(monsterPosition[0] * cellWidth);
            chupacabra.setRealY(monsterPosition[1] * cellHeight);
            chupacabra.setController(this);
            monsters.add(chupacabra);
        }

        for (int i = 0; i < 4; i++) {
            int[] monsterPosition = getRandomValidPosition();

            Image goraTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/gora.png")));
            Gora gora = new Gora(monsterPosition[0], monsterPosition[1], goraTexture, player);
            gora.setRealX(monsterPosition[0] * cellWidth);
            gora.setRealY(monsterPosition[1] * cellHeight);
            gora.setController(this);
            monsters.add(gora);
        }


        drawMaze();
    }
    private void drawMaze() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        maze.draw(gc);
        drawEntity(player,gc);
        for (Monster monster : monsters) {
            drawEntity(monster, gc);
        }

    }

    private int[] getRandomValidPosition() {
        int cols = maze.getCols();
        int rows = maze.getRows();
        int x, y;

        do {
            x = (int) (Math.random() * cols);
            y = (int) (Math.random() * rows);

        } while (!maze.isCellFree(x, y) || isPositionOccupiedByPlayerOrMonster(x, y));
        return new int[]{x, y};
    }

    private boolean isPositionOccupiedByPlayerOrMonster(int x, int y) {
        // Vérifier si le joueur occupe cette position
        if (player.getX() == x && player.getY() == y) {
            return true;
        }

        // Vérifier si un monstre occupe cette position
        for (Monster monster : monsters) {
            if (monster.getX() == x && monster.getY() == y) {
                return true;
            }
        }

        return false; // La position est libre
    }
    /**
     * Quitte le jeu
     */
    @FXML
    public void exitGame() {
        Stage stage = (Stage) exit.getScene().getWindow();
        stage.close();
    }
}

