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

    public void initialize() {
        canvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                canvas.widthProperty().bind(newScene.widthProperty());
                canvas.heightProperty().bind(newScene.heightProperty());
                newScene.windowProperty().addListener((obs2, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        updateGrid(); // Initialiser le jeu une seule fois
                    }
                });

                // Activer les événements clavier
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

        mazeContainer.widthProperty().addListener((obs, oldVal, newVal) -> adjustCanvasSize());
        mazeContainer.heightProperty().addListener((obs, oldVal, newVal) -> adjustCanvasSize());
    }

    private void adjustCanvasSize() {
        double screenWidth = canvas.getWidth();
        double screenHeight = canvas.getHeight();
        int cols = maze.getCols();
        int rows = maze.getRows();

        // Mettre à jour les dimensions des cellules
        int cellWidth = (int) Math.round(screenWidth / cols);
        int cellHeight = (int) Math.round(screenHeight / rows);

        // Redessiner le labyrinthe avec les nouvelles dimensions
        drawMaze();
    }
    private void startMovement() {
        if (!isMoving && (currentDx != 0 || currentDy != 0)) {
            movePlayer(currentDx, currentDy);
        }
    }

    private void startMonsterMovement() {
        ;
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

        final double speed = 300;
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

    private void drawEntity(Entity entity, GraphicsContext gc) {

        int cellWidth = (int) Math.round(canvas.getWidth() / maze.getCols());
        int cellHeight = (int) Math.round(canvas.getHeight() / maze.getRows());
        double px, py;

        if (entity instanceof Monster){
            px = ((Monster) entity).getCurrentX();
            py = ((Monster) entity).getCurrentY();
        }else{
            px = entity.getRealX();
            py = entity.getRealY();
        }

        if (entity.getTexture() != null) {
            gc.drawImage(entity.getTexture(), px, py, cellWidth, cellHeight);
        } else {
            gc.setFill(Color.GRAY);
            gc.fillRect(px, py, cellWidth, cellHeight);
        }
    }

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

        Image wallTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/metal.png")));
        Image floorTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/pave.png")));
        Image doorTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/porte.jpg")));

        maze = new MazeGenerator(cols, rows, cellWidth, cellHeight, wallTexture, floorTexture, doorTexture);
        while (maze.generateStep());
        player = new Player(100, 0, "Djamel", new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-right.png"))));

        monsters.clear();


        for (int i = 0; i < 2; i++) {
            int[] monsterPosition = getRandomValidPosition();

            Image dragonTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/dragon.png")));
            Dragon dragon = new Dragon(monsterPosition[0], monsterPosition[1], dragonTexture,cellWidth, cellHeight);
            dragon.setRealX(monsterPosition[0] * cellWidth);
            dragon.setRealY(monsterPosition[1] * cellHeight);
            dragon.setController(this);
            monsters.add(dragon);
        }
        for (int i = 0; i < 5; i++) {
            int[] monsterPosition = getRandomValidPosition();

            Image chupacabraTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/chupacabra.png")));
            Chupacabra chupacabra = new Chupacabra(monsterPosition[0], monsterPosition[1], chupacabraTexture,cellWidth, cellHeight);
            chupacabra.setRealX(monsterPosition[0] * cellWidth);
            chupacabra.setRealY(monsterPosition[1] * cellHeight);
            chupacabra.setController(this);
            monsters.add(chupacabra);
        }
        for (int i = 0; i < 3; i++) {
            int[] monsterPosition = getRandomValidPosition();

            Image goraTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/gora.png")));
            Gora gora = new Gora(monsterPosition[0], monsterPosition[1], goraTexture,player, cellWidth, cellHeight);
            gora.setRealX(monsterPosition[0] * cellWidth);
            gora.setRealY(monsterPosition[1] * cellHeight);
            gora.setController(this);
            monsters.add(gora);
        }


        drawMaze();
    }

    private void drawMaze() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Effacer tout le canevas

        // Dessiner le labyrinthe
        maze.draw(gc);

        // Dessiner le joueur
        drawEntity(player, gc);

        for (Monster monster : monsters) {
            drawEntity(monster, gc);
            monster.autoMove(maze);
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
        if (player.getX() == x && player.getY() == y) {
            return true;
        }

        for (Monster monster : monsters) {
            if (monster.getX() == x && monster.getY() == y) {
                return true;
            }
        }

        return false;
    }

    public MazeGenerator getMaze() {
        return maze;
    }

    @FXML
    public void exitGame() {
        Stage stage = (Stage) exit.getScene().getWindow();
        stage.close();
    }
}
