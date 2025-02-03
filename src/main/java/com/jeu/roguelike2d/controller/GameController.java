package com.jeu.roguelike2d.controller;

import com.jeu.roguelike2d.model.Entity;
import com.jeu.roguelike2d.model.MazeGenerator;
import com.jeu.roguelike2d.model.Player;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import java.util.Objects;

public class GameController {
    @FXML private Canvas canvas;
    @FXML private HBox topBar;
    @FXML private VBox sideBar;
    @FXML private Button exit;
    @FXML private Pane mazeContainer;

    private MazeGenerator maze;
    private Player player;

    private int currentDx = 0;
    private int currentDy = 0;
    private boolean isMoving = false;

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

    private void movePlayer(int dx, int dy) {
        if (player.move(dx, dy, maze)) {
            isMoving = true; // Marquer que l'animation est en cours
            animateEntityMovement(player, dx, dy, this::startMovement);
        } else {
            isMoving = false; // Si le déplacement échoue, arrêter le mouvement
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
                    // Forcer l'alignement exact à la fin de l'animation
                    entity.setRealX(targetX);
                    entity.setRealY(targetY);
                    this.stop();
                    drawMaze();

                    isMoving = false;
                    onComplete.run();
                } else {
                    // Calculer la position intermédiaire
                    double newX = startX + (targetX - startX) * (progress / Math.hypot(targetX - startX, targetY - startY));
                    double newY = startY + (targetY - startY) * (progress / Math.hypot(targetX - startX, targetY - startY));
                    entity.setRealX(newX);
                    entity.setRealY(newY);
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

        // Vérifier si l'entité a une texture
        if (entity.getTexture() != null) {
            gc.drawImage(entity.getTexture(), px, py, cellWidth, cellHeight);
        } else {
            // Si pas de texture, dessiner un rectangle par défaut
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
        drawMaze();
    }
    private void drawMaze() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        maze.draw(gc);
        drawEntity(player,gc);

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


//import com.jeu.roguelike2d.model.Monster;
//import com.jeu.roguelike2d.model.Player;
//import com.jeu.roguelike2d.model.Projectile;
//import com.jeu.roguelike2d.model.MazeGenerator;
//import javafx.animation.AnimationTimer;
//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import javafx.scene.Scene;
//import javafx.scene.canvas.Canvas;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.control.Button;
//import javafx.scene.image.Image;
//import javafx.scene.input.KeyCode;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.Pane;
//import javafx.scene.layout.VBox;
//import javafx.scene.paint.Color;
//import javafx.stage.Stage;
//
//import java.util.*;
//
//public class GameController {
//    @FXML private Canvas canvas;
//    @FXML private HBox topBar;
//    @FXML private VBox sideBar;
//    @FXML private Button exit;
//    @FXML private Pane mazeContainer;
//    private List<Projectile> projectiles = new ArrayList<>();
//
//    private MazeGenerator maze;
//    private AnimationTimer mazeGenerationTimer;
//    private AnimationTimer gameLoopTimer;
//    private Player player;
//    public static int CELL_SIZE = 60;
//    private double currentX, currentY;
//    private double targetX, targetY;
//
//    private static final double SPEED = 5.0; // Vitesse constante (en pixels par frame)
//    private Queue<Runnable> movementQueue = new LinkedList<>(); // File d'attente pour les mouvements
//
//    private List<Monster> monsters = new ArrayList<>(); // Liste des monstres
//    private static final int MAX_MONSTERS = 5; // Nombre maximum de monstres
//
//    public void initialize() {
//        Image upTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-up.png")));
//        Image downTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-front.png")));
//        Image leftTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-left.png")));
//        Image rightTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-right.png")));
//
//        player = new Player(0, 0, upTexture, downTexture, leftTexture, rightTexture);
//        currentX = player.getX() * CELL_SIZE;
//        currentY = player.getY() * CELL_SIZE;
//        targetX = currentX;
//        targetY = currentY;
//
//        topBar.sceneProperty().addListener((obs, oldScene, newScene) -> {
//            if (newScene != null) {
//                newScene.widthProperty().addListener((o, oldVal, newVal) -> resizeUI(newScene));
//                newScene.heightProperty().addListener((o, oldVal, newVal) -> resizeUI(newScene));
//                newScene.setOnKeyPressed(event -> handlePlayerMovement(event.getCode()));
//                generateMaze();
//                Platform.runLater(() -> newScene.getRoot().requestFocus());
//            }
//        });
//    }
//
//    private void resizeUI(Scene scene) {
//        canvas.setWidth(scene.getWidth());
//        canvas.setHeight(scene.getHeight());
//        generateMaze();
//    }
//
//    private void generateMaze() {
//        if (canvas.getWidth() == 0 || canvas.getHeight() == 0) return;
//
//        int cols = (int) (canvas.getWidth() / CELL_SIZE);
//        int rows = (int) (canvas.getHeight() / CELL_SIZE);
//
//        if (cols == 0 || rows == 0) return;
//
//        maze = new MazeGenerator(
//                cols,
//                rows,
//                CELL_SIZE,
//                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/briques.jpg"))),
//                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/pave.png"))),
//                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/porte.jpg")))
//        );
//
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//
//        // Timer pour générer le labyrinthe
//        mazeGenerationTimer = new AnimationTimer() {
//            @Override
//            public void handle(long now) {
//                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
//                if (!maze.generateStep()) {
//                    stop(); // Arrêter le timer une fois le labyrinthe généré
//                    startGameLoop(gc); // Démarrer la boucle principale du jeu
//                }
//                maze.draw(gc);
//            }
//        };
//        generateMonsters();
//        mazeGenerationTimer.start();
//    }
//
//    private void startGameLoop(GraphicsContext gc) {
//        gameLoopTimer = new AnimationTimer() {
//            @Override
//            public void handle(long now) {
//                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
//                maze.draw(gc);
//                updateGame(); // Mise à jour complète du jeu incluant les tirs des monstres
//                drawMonsters(gc);
//                updatePlayerPosition();
//                drawPlayer(gc);
//                drawProjectiles(gc);
//            }
//        };
//        gameLoopTimer.start();
//    }
//    private void updateGame() {
//        updateProjectiles();
//        updateMonsters();
//
//        // Faire tirer les monstres
//        for (Monster monster : monsters) {
//            if (monster.isAlive()) {
//                Projectile projectile = monster.shoot(currentX, currentY);
//                if (projectile != null) {
//                    projectiles.add(projectile);
//                }
//            }
//        }
//    }
//
//    private void updateMonsters() {
//        for (Monster monster : monsters) {
//            if (monster.isAlive()) {
//                monster.move(maze, CELL_SIZE);
//            }
//        }
//    }
//    private void drawPlayer(GraphicsContext gc) {
//        double playerX = currentX;
//        double playerY = currentY;
//        int playerWidth = (int) (CELL_SIZE * 0.8);
//        int playerHeight = (int) (CELL_SIZE * 0.7);
//        int offsetX = (CELL_SIZE - playerWidth) / 2;
//        int offsetY = (CELL_SIZE - playerHeight) / 2;
//
//        gc.drawImage(player.getCurrentTexture(), playerX + offsetX, playerY + offsetY, playerWidth, playerHeight);
//    }
//
//    private void drawMonsters(GraphicsContext gc) {
//        for (Monster monster : monsters) {
//            if (monster.isAlive()) {
//                double monsterX = monster.getCurrentX();
//                double monsterY = monster.getCurrentY();
//                int monsterWidth = (int) (CELL_SIZE * 0.8);
//                int monsterHeight = (int) (CELL_SIZE * 0.8);
//                int offsetX = (CELL_SIZE - monsterWidth) / 2;
//                int offsetY = (CELL_SIZE - monsterHeight) / 2;
//
//                gc.drawImage(monster.getTexture(), monsterX + offsetX, monsterY + offsetY, monsterWidth, monsterHeight);
//            }
//        }
//    }
//    private void generateMonsters() {
//        Random random = new Random();
//        int cols = (int) (canvas.getWidth() / CELL_SIZE);
//        int rows = (int) (canvas.getHeight() / CELL_SIZE);
//
//        for (int i = 0; i < MAX_MONSTERS; i++) {
//            int monsterX, monsterY;
//            do {
//                monsterX = random.nextInt(cols);
//                monsterY = random.nextInt(rows);
//            } while (!maze.canMove(monsterX, monsterY, 0, 0));
//
//            Image monsterTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/monster.png")));
//            monsters.add(new Monster(monsterX, monsterY, monsterTexture, 100, maze)); // Passer le labyrinthe
//        }
//    }
//    private void updatePlayerPosition() {
//        if (Math.abs(currentX - targetX) <= SPEED && Math.abs(currentY - targetY) <= SPEED) {
//            currentX = targetX;
//            currentY = targetY;
//
//
//            if (!movementQueue.isEmpty()) {
//                movementQueue.poll().run();
//            }
//        } else {
//            // Déplacer le joueur vers la cible à une vitesse constante
//            if (currentX < targetX) currentX += SPEED;
//            if (currentX > targetX) currentX -= SPEED;
//            if (currentY < targetY) currentY += SPEED;
//            if (currentY > targetY) currentY -= SPEED;
//        }
//    }
//
//    private void handlePlayerMovement(KeyCode keyCode) {
//        if (keyCode == KeyCode.SPACE) {
//            Projectile projectile = player.shoot();
//            if (projectile != null) {
//                projectiles.add(projectile);
//            }
//            return;
//        }
//
//        Runnable movement = null;
//        switch (keyCode) {
//            case UP:
//                if (maze.canMove(player.getX(), player.getY(), 0, -1)) {
//                    movement = () -> {
//                        player.setY(player.getY() - 1);
//                        player.setDirection(Player.Direction.UP);
//                        targetY = player.getY() * CELL_SIZE;
//                    };
//                }
//                player.setDirection(Player.Direction.UP);
//                break;
//            case DOWN:
//                if (maze.canMove(player.getX(), player.getY(), 0, 1)) {
//                    movement = () -> {
//                        player.setY(player.getY() + 1);
//                        player.setDirection(Player.Direction.DOWN);
//                        targetY = player.getY() * CELL_SIZE;
//                    };
//                }
//                player.setDirection(Player.Direction.DOWN);
//                break;
//            case LEFT:
//                if (maze.canMove(player.getX(), player.getY(), -1, 0)) {
//                    movement = () -> {
//                        player.setX(player.getX() - 1);
//                        player.setDirection(Player.Direction.LEFT);
//                        targetX = player.getX() * CELL_SIZE;
//                    };
//                }
//                player.setDirection(Player.Direction.LEFT);
//                break;
//            case RIGHT:
//                if (maze.canMove(player.getX(), player.getY(), 1, 0)) {
//                    movement = () -> {
//                        player.setX(player.getX() + 1);
//                        player.setDirection(Player.Direction.RIGHT);
//                        targetX = player.getX() * CELL_SIZE;
//                    };
//                }
//                player.setDirection(Player.Direction.RIGHT);
//                break;
//        }
//
//        if (movement != null) {
//            if (movementQueue.isEmpty()) {
//                movement.run();
//            } else {
//                movementQueue.add(movement);
//            }
//        }
//    }
//
//
//    private void updateProjectiles() {
//        Iterator<Projectile> it = projectiles.iterator();
//        while (it.hasNext()) {
//            Projectile projectile = it.next();
//
//            int currentGridX = (int) (projectile.getX() / CELL_SIZE);
//            int currentGridY = (int) (projectile.getY() / CELL_SIZE);
//
//            double newX = projectile.getX() + projectile.getDirectionX() * Projectile.getSpeed();
//            double newY = projectile.getY() + projectile.getDirectionY() * Projectile.getSpeed();
//
//            int newGridX = (int) (newX / CELL_SIZE);
//            int newGridY = (int) (newY / CELL_SIZE);
//
//            if (newGridX < 0 || newGridY < 0 || newGridX >= maze.getWidth() || newGridY >= maze.getHeight()) {
//                it.remove();
//                continue;
//            }
//
//            // Vérifier s'il y a un mur dans la cellule cible
//            boolean wallCollision = false;
//
//            // Vérifier la cellule actuelle
//            if (!maze.canMove(currentGridX, currentGridY, 0, 0)) {
//                wallCollision = true;
//                System.out.println("collision");
//            }
//
//            // Vérifier la cellule cible
//            if (!maze.canMove(newGridX, newGridY, 0, 0)) {
//                wallCollision = true;
//                System.out.println("collision");
//            }
//
//            // Vérifier les cellules intermédiaires si le projectile traverse en diagonale
//            if (currentGridX != newGridX && currentGridY != newGridY) {
//                if (!maze.canMove(currentGridX, newGridY, 0, 0) ||
//                        !maze.canMove(newGridX, currentGridY, 0, 0)) {
//                    wallCollision = true;
//                    System.out.println("collision");
//                }
//            }
//
//            // Si collision avec un mur, supprimer le projectile
//            if (wallCollision) {
//                it.remove();
//                continue;
//            }
//
//            // Mettre à jour la position du projectile
//            projectile.setPosition(newX, newY);
//
//            // Vérifier les collisions avec les monstres (pour les projectiles du joueur)
//            if (projectile.isPlayerProjectile()) {
//                for (Monster monster : monsters) {
//                    if (monster.isAlive() && projectile.collidesWith(
//                            monster.getCurrentX() + CELL_SIZE/2,
//                            monster.getCurrentY() + CELL_SIZE/2,
//                            CELL_SIZE/2)) {
//                        monster.takeDamage(projectile.getDamage());
//                        it.remove();
//                        break;
//                    }
//                }
//            } else {
//                // Vérifier la collision avec le joueur (pour les projectiles des monstres)
//                if (projectile.collidesWith(
//                        currentX + CELL_SIZE/2,
//                        currentY + CELL_SIZE/2,
//                        CELL_SIZE/2)) {
//                    player.takeDamage(projectile.getDamage());
//                    it.remove();
//                }
//            }
//        }
//    }
//
//    private void drawProjectiles(GraphicsContext gc) {
//        gc.setFill(Color.YELLOW); // Couleur plus visible pour les projectiles
//        for (Projectile projectile : projectiles) {
//            if (projectile.isPlayerProjectile()) {
//                gc.setFill(Color.YELLOW); // Projectiles du joueur en jaune
//            } else {
//                gc.setFill(Color.RED); // Projectiles des monstres en rouge
//            }
//            gc.fillOval(
//                    projectile.getX() - 4,
//                    projectile.getY() - 4,
//                    8, 8
//            );
//        }
//    }
//    private void checkIfPlayerReachedDoor() {
//        if (maze.getDoorCell() != null && player.getX() == maze.getDoorCell().getX() && player.getY() == maze.getDoorCell().getY()) {
//            System.out.println("Player reached the door!");
//            // Vous pouvez ajouter ici une action spécifique lorsque le joueur atteint la porte
//        }
//    }
//
//    @FXML
//    public void exitGame() {
//        Stage stage = (Stage) exit.getScene().getWindow();
//        stage.close();
//    }
//}