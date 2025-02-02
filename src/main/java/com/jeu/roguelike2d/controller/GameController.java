package com.jeu.roguelike2d.controller;

import com.jeu.roguelike2d.model.Monster;
import com.jeu.roguelike2d.model.Player;
import com.jeu.roguelike2d.model.Projectile;
import com.jeu.roguelike2d.utils.MazeGenerator;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.*;

public class GameController {
    @FXML private Canvas canvas;
    @FXML private HBox topBar;
    @FXML private VBox sideBar;
    @FXML private Button exit;
    @FXML private Pane mazeContainer;
    private List<Projectile> projectiles = new ArrayList<>();

    private MazeGenerator maze;
    private AnimationTimer mazeGenerationTimer; // Pour générer le labyrinthe
    private AnimationTimer gameLoopTimer; // Pour mettre à jour le joueur et le rendu
    private Player player;
    public static int CELL_SIZE = 60;
    private double currentX, currentY; // Position actuelle du joueur
    private double targetX, targetY; // Position cible du joueur

    private static final double SPEED = 5.0; // Vitesse constante (en pixels par frame)
    private Queue<Runnable> movementQueue = new LinkedList<>(); // File d'attente pour les mouvements

    private List<Monster> monsters = new ArrayList<>(); // Liste des monstres
    private static final int MAX_MONSTERS = 5; // Nombre maximum de monstres

    public void initialize() {
        Image upTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-up.png")));
        Image downTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-front.png")));
        Image leftTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-left.png")));
        Image rightTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-right.png")));

        player = new Player(0, 0, upTexture, downTexture, leftTexture, rightTexture);
        currentX = player.getX() * CELL_SIZE;
        currentY = player.getY() * CELL_SIZE;
        targetX = currentX;
        targetY = currentY;

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

        int cols = (int) (canvas.getWidth() / CELL_SIZE);
        int rows = (int) (canvas.getHeight() / CELL_SIZE);

        if (cols == 0 || rows == 0) return;

        maze = new MazeGenerator(
                cols,
                rows,
                CELL_SIZE,
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/briques.jpg"))),
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/pave.png"))),
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/porte.jpg")))
        );

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Timer pour générer le labyrinthe
        mazeGenerationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                if (!maze.generateStep()) {
                    stop(); // Arrêter le timer une fois le labyrinthe généré
                    startGameLoop(gc); // Démarrer la boucle principale du jeu
                }
                maze.draw(gc);
            }
        };
        generateMonsters();
        mazeGenerationTimer.start();
    }

    private void startGameLoop(GraphicsContext gc) {
        gameLoopTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                maze.draw(gc);
                updateGame(); // Mise à jour complète du jeu incluant les tirs des monstres
                drawMonsters(gc);
                updatePlayerPosition();
                drawPlayer(gc);
                drawProjectiles(gc);
            }
        };
        gameLoopTimer.start();
    }
    private void updateGame() {
        updateProjectiles();
        updateMonsters();

        // Faire tirer les monstres
        for (Monster monster : monsters) {
            if (monster.isAlive()) {
                Projectile projectile = monster.shoot(currentX, currentY);
                if (projectile != null) {
                    projectiles.add(projectile);
                }
            }
        }
    }

    private void updateMonsters() {
        for (Monster monster : monsters) {
            if (monster.isAlive()) {
                monster.move(maze, CELL_SIZE);
            }
        }
    }
    private void drawPlayer(GraphicsContext gc) {
        double playerX = currentX;
        double playerY = currentY;
        int playerWidth = (int) (CELL_SIZE * 0.8);
        int playerHeight = (int) (CELL_SIZE * 0.7);
        int offsetX = (CELL_SIZE - playerWidth) / 2;
        int offsetY = (CELL_SIZE - playerHeight) / 2;

        gc.drawImage(player.getCurrentTexture(), playerX + offsetX, playerY + offsetY, playerWidth, playerHeight);
    }

    private void drawMonsters(GraphicsContext gc) {
        for (Monster monster : monsters) {
            if (monster.isAlive()) {
                double monsterX = monster.getCurrentX();
                double monsterY = monster.getCurrentY();
                int monsterWidth = (int) (CELL_SIZE * 0.8);
                int monsterHeight = (int) (CELL_SIZE * 0.8);
                int offsetX = (CELL_SIZE - monsterWidth) / 2;
                int offsetY = (CELL_SIZE - monsterHeight) / 2;

                gc.drawImage(monster.getTexture(), monsterX + offsetX, monsterY + offsetY, monsterWidth, monsterHeight);
            }
        }
    }
    private void generateMonsters() {
        Random random = new Random();
        int cols = (int) (canvas.getWidth() / CELL_SIZE);
        int rows = (int) (canvas.getHeight() / CELL_SIZE);

        for (int i = 0; i < MAX_MONSTERS; i++) {
            int monsterX, monsterY;
            do {
                monsterX = random.nextInt(cols);
                monsterY = random.nextInt(rows);
            } while (!maze.canMove(monsterX, monsterY, 0, 0)); // Assure que le monstre est placé sur une case valide

            Image monsterTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/monster.png")));
            monsters.add(new Monster(monsterX, monsterY, monsterTexture, 100, maze)); // Passer le labyrinthe
        }
    }
    private void updatePlayerPosition() {
        if (Math.abs(currentX - targetX) <= SPEED && Math.abs(currentY - targetY) <= SPEED) {
            currentX = targetX; // Forcer l'alignement parfait avec la grille
            currentY = targetY;

            // Passer au prochain mouvement dans la file d'attente
            if (!movementQueue.isEmpty()) {
                movementQueue.poll().run(); // Exécuter le prochain mouvement
            }
        } else {
            // Déplacer le joueur vers la cible à une vitesse constante
            if (currentX < targetX) currentX += SPEED;
            if (currentX > targetX) currentX -= SPEED;
            if (currentY < targetY) currentY += SPEED;
            if (currentY > targetY) currentY -= SPEED;
        }
    }

    private void handlePlayerMovement(KeyCode keyCode) {
        if (keyCode == KeyCode.SPACE) {
            Projectile projectile = player.shoot();
            if (projectile != null) {
                projectiles.add(projectile);
                System.out.println("Nouveau projectile ajouté");
            }
            return;
        }

        Runnable movement = null;
        switch (keyCode) {
            case UP:
                if (maze.canMove(player.getX(), player.getY(), 0, -1)) {
                    movement = () -> {
                        player.setY(player.getY() - 1);
                        player.setDirection(Player.Direction.UP);
                        player.setDirection(Player.Direction.UP);
                        targetY = player.getY() * CELL_SIZE;
                    };
                }
                player.setDirection(Player.Direction.UP);
                break;
            case DOWN:
                if (maze.canMove(player.getX(), player.getY(), 0, 1)) {
                    movement = () -> {
                        player.setY(player.getY() + 1);
                        player.setDirection(Player.Direction.DOWN);
                        targetY = player.getY() * CELL_SIZE;
                    };
                }
                player.setDirection(Player.Direction.DOWN);
                break;
            case LEFT:
                if (maze.canMove(player.getX(), player.getY(), -1, 0)) {
                    movement = () -> {
                        player.setX(player.getX() - 1);
                        player.setDirection(Player.Direction.LEFT);
                        targetX = player.getX() * CELL_SIZE;
                    };
                }
                player.setDirection(Player.Direction.LEFT);
                break;
            case RIGHT:
                if (maze.canMove(player.getX(), player.getY(), 1, 0)) {
                    movement = () -> {
                        player.setX(player.getX() + 1);
                        player.setDirection(Player.Direction.RIGHT);
                        targetX = player.getX() * CELL_SIZE;
                    };
                }
                player.setDirection(Player.Direction.RIGHT);
                break;
        }

        if (movement != null) {
            if (movementQueue.isEmpty()) {
                movement.run();
            } else {
                movementQueue.add(movement);
            }
        }
    }


    private void updateProjectiles() {
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile projectile = it.next();
            projectile.update();

            // Vérification des collisions avec les murs
            int gridX = (int) (projectile.getX() / CELL_SIZE);
            int gridY = (int) (projectile.getY() / CELL_SIZE);

            // Vérifier si le projectile est sorti du labyrinthe
            if (gridX < 0 || gridY < 0 || gridX >= maze.getWidth() || gridY >= maze.getHeight()) {
                it.remove();
                continue;
            }

            // Vérifier si le projectile a heurté un mur
            if (!maze.canMove(gridX, gridY, 0, 0)) {
                it.remove();
                continue;
            }

            // Vérifier les collisions avec les monstres si c'est un projectile du joueur
            if (projectile.isPlayerProjectile()) {
                for (Monster monster : monsters) {
                    if (monster.isAlive() && projectile.collidesWith(
                            monster.getCurrentX() + CELL_SIZE/2,
                            monster.getCurrentY() + CELL_SIZE/2,
                            CELL_SIZE/2)) {
                        monster.takeDamage(projectile.getDamage());
                        it.remove();
                        break;
                    }
                }
            } else {
                // Vérifier la collision avec le joueur si c'est un projectile de monstre
                if (projectile.collidesWith(
                        currentX + CELL_SIZE/2,
                        currentY + CELL_SIZE/2,
                        CELL_SIZE/2)) {
                    player.takeDamage(projectile.getDamage());
                    it.remove();
                }
            }
        }
    }

    private void drawProjectiles(GraphicsContext gc) {
        gc.setFill(Color.YELLOW); // Couleur plus visible pour les projectiles
        for (Projectile projectile : projectiles) {
            if (projectile.isPlayerProjectile()) {
                gc.setFill(Color.YELLOW); // Projectiles du joueur en jaune
            } else {
                gc.setFill(Color.RED); // Projectiles des monstres en rouge
            }
            gc.fillOval(
                    projectile.getX() - 4,
                    projectile.getY() - 4,
                    8, 8
            );
        }
    }
    private void checkIfPlayerReachedDoor() {
        if (maze.getDoorCell() != null && player.getX() == maze.getDoorCell().getX() && player.getY() == maze.getDoorCell().getY()) {
            System.out.println("Player reached the door!");
            // Vous pouvez ajouter ici une action spécifique lorsque le joueur atteint la porte
        }
    }

    @FXML
    public void exitGame() {
        Stage stage = (Stage) exit.getScene().getWindow();
        stage.close();
    }
}