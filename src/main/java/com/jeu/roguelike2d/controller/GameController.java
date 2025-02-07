package com.jeu.roguelike2d.controller;

import com.jeu.roguelike2d.model.*;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class GameController {
    @FXML private Canvas canvas;
    @FXML private HBox topBar;
    @FXML private ProgressBar healthProgress;
    @FXML private ProgressBar energyProgress;
    @FXML private Label playerNameLabel;
    @FXML private Label timeLabel;
    @FXML private VBox sideBar;
    @FXML private Button exit;
    @FXML private Pane mazeContainer;

    private MazeGenerator maze;
    private Player player;

    private boolean win = false;
    private List<Monster> monsters = new ArrayList<>();
    private List<StaticObject> objects = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private Image bulletTexture;
    private int currentDx = 0;
    private int currentDy = 0;
    private int lastDx = 0;
    private int lastDy = 0;
    private boolean isMoving = false;
    private AnimationTimer monsterMovementAnimation;
    private List<Monster> collidedMonsters = new ArrayList<>();
    private List<StaticObject> triggeredTraps = new ArrayList<>();
    private AnimationTimer timerAnimation;
    private Long startTime;

    private static final double PLAY_DURATION = 2.8;
    private static final double PAUSE_DURATION = 7.5;
    private double lastPosition = 0;
    MediaPlayer soldierSounds;
    public void initialize() {
        String soundPath = getClass().getResource("/com/jeu/roguelike2d/sons/radio-soldier-announcer-pack.mp3").toString();
        Media sound = new Media(soundPath);
        soldierSounds = new MediaPlayer(sound);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, event -> playSegment()), // Démarrer immédiatement
                new KeyFrame(Duration.seconds(PLAY_DURATION), event -> pauseSegment()), // Pause après 2.5s
                new KeyFrame(Duration.seconds(PLAY_DURATION + PAUSE_DURATION), event -> playSegment()) // Reprendre après 7.5s
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        bulletTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/fireball-fire.gif")));
        canvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                startTime = System.nanoTime();
                startTimer();
                canvas.widthProperty().bind(newScene.widthProperty());
                canvas.heightProperty().bind(newScene.heightProperty());
                newScene.windowProperty().addListener((obs2, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        updateGrid();
                    }
                });


                canvas.setOnKeyPressed(event -> {
                    switch (event.getCode()) {
                        case UP -> { currentDx = 0; currentDy = -1; lastDx = 0; lastDy = -1; }
                        case DOWN -> { currentDx = 0; currentDy = 1; lastDx = 0; lastDy = 1; }
                        case LEFT -> { currentDx = -1; currentDy = 0; lastDx = -1; lastDy = 0; }
                        case RIGHT -> { currentDx = 1; currentDy = 0; lastDx = 1; lastDy = 0; }
                        case SPACE -> {
                            shootProjectile();
                            playGunShotSound();
                        }
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

    private void playSegment() {
        soldierSounds.setStartTime(Duration.seconds(lastPosition));
        soldierSounds.play();
    }

    private void pauseSegment() {
        lastPosition = soldierSounds.getCurrentTime().toSeconds();
        soldierSounds.pause();
    }
    private void adjustCanvasSize() {
        double screenWidth = canvas.getWidth();
        double screenHeight = canvas.getHeight();
        int cols = maze.getCols();
        int rows = maze.getRows();

        // Mettre à jour les dimensions des cellules
        int cellWidth = (int) Math.round(screenWidth / cols);
        int cellHeight = (int) Math.round(screenHeight / rows);
        drawMaze();
    }

    private void shootProjectile() {
        int startX = player.getX();
        int startY = player.getY();
        int dx = lastDx;
        int dy = lastDy;

        if (dx == 0 && dy == 0) {
            System.out.println("Cannot shoot: no direction selected.");
            return;
        }

        int cellWidth = (int) Math.round(canvas.getWidth() / maze.getCols());
        int cellHeight = (int) Math.round(canvas.getHeight() / maze.getRows());
        double startXPixel = (startX + 0.5) * cellWidth;
        double startYPixel = (startY + 0.5) * cellHeight;

        // Créer un nouveau projectile
        Projectile projectile = new Projectile(startXPixel, startYPixel, dx, dy, bulletTexture);
        projectiles.add(projectile);


//        SoundPlayer.playShootSound();
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
                checkInteractions();
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
        }else if (entity instanceof Player){
            px = entity.getRealX();
            py = entity.getRealY();
        }else{
            px = entity.getX() * cellWidth;
            py = entity.getY() * cellHeight;
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

        Image wallTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/rust.jpg")));
        Image floorTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/metal-noir.png")));
        Image doorTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/porte.jpg")));

        maze = new MazeGenerator(cols, rows, cellWidth, cellHeight, wallTexture, floorTexture, doorTexture);
        while (maze.generateStep());
        player = new Player(100, 0, playerNameLabel.getText(), new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-right.png"))));
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


            int[] keyPosition = getRandomValidPosition();

            Image keyTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/key.gif")));
            Reward key = new Reward(keyPosition[0], keyPosition[1], "key",keyTexture,0);
            objects.add(key);

        for (int i = 0; i < 4; i++) {
            int[] objetPosition = getRandomValidPosition();

            Image blueDiamondTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/blue-diamond.gif")));
            Reward blueDiamond = new Reward(objetPosition[0], objetPosition[1], "Blue Diamond",blueDiamondTexture,20);
            objects.add(blueDiamond);
        }
        for (int i = 0; i < 2; i++) {
            int[] objetPosition = getRandomValidPosition();

            Image diamonCollectionTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/diamonds-collection.gif")));
            Reward diamondCollection = new Reward(objetPosition[0], objetPosition[1], "Diamond collection",diamonCollectionTexture,35);
            objects.add(diamondCollection);
        }
        for (int i = 0; i < 3; i++) {
            int[] objetPosition = getRandomValidPosition();

            Image heartTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/heart.gif")));
            Reward heart = new Reward(objetPosition[0], objetPosition[1], "Heart",heartTexture,35);
            objects.add(heart);
        }
        for (int i = 0; i < 2; i++) {
            int[] objetPosition = getRandomValidPosition();

            Image holeTrapTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/hole-trap.gif")));
            Trap holeTrap = new Trap(objetPosition[0], objetPosition[1], "Hole Trap",holeTrapTexture,40);
            objects.add(holeTrap);
        }
        for (int i = 0; i < 4; i++) {
            int[] objetPosition = getRandomValidPosition();

            Image electricTrapTexture = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/electrical-trap.gif")));
            Trap electricTrap = new Trap(objetPosition[0], objetPosition[1], "Electric Trap",electricTrapTexture,10);
            objects.add(electricTrap);
        }

        drawMaze();
    }

    private void checkInteractions() {

        for (Iterator<StaticObject> iterator = objects.iterator(); iterator.hasNext(); ) {
            StaticObject object = iterator.next();
            if (player.getX() == object.getX() && player.getY() == object.getY()) {
                if (object instanceof Trap) {
                    if (!triggeredTraps.contains(object)) {
                        object.onContact(player);
                        triggeredTraps.add(object);
                        playTrapSound();
                        if (!player.isAlive()){  win=false; stopGame();}
                    }
                } else if (object instanceof Reward) {
                    if (object.getName().equals("key")){
                        player.setHasKey(true);
                    }
                    object.onContact(player);
                    iterator.remove();
                    playRewardSound();
                }
            } else {
                triggeredTraps.remove(object);
            }
        }

        for (Iterator<Monster> iterator = monsters.iterator(); iterator.hasNext(); ) {
            Monster monster = iterator.next();
            if (player.getX() == monster.getX() && player.getY() == monster.getY()) {
                if (!collidedMonsters.contains(monster)) {
                    handlePlayerMonsterCollision(player, monster);
                    playTrapSound();
                    collidedMonsters.add(monster);
                }
            } else {
                collidedMonsters.remove(monster);
            }

            if (!monster.isAlive()) {
                iterator.remove();
            }
        }
    }
    private void handlePlayerMonsterCollision(Player player, Monster monster) {
        player.takeDamage(monster.getDamage());
        monster.takeDamage(player.getDamage());

        if (!monster.isAlive()) {
            monsters.remove(monster);
        }

        if (!player.isAlive()) {
            win = false;
            stopGame();
        }
    }
    private void stopGame() {
        if (monsterMovementAnimation != null) {
            monsterMovementAnimation.stop();
        }
        if (timerAnimation != null) {
            timerAnimation.stop();
        }
        if (win) {
            playCongratulationSound();
        }else {
            playLooseSound();
        }
        soldierSounds.stop();
        Platform.runLater(this::showLevelDialog);
        drawMaze();
    }
    private void drawMaze() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        maze.draw(gc);

        for (StaticObject object : objects) {
            drawEntity(object, gc);
        }
        drawEntity(player, gc);
        healthProgress.setProgress((double) player.getHealth() / 100);
        energyProgress.setProgress((double) player.getEnergy() / 100);

        for (Monster monster : monsters) {
            drawEntity(monster, gc);
            monster.autoMove(maze);
        }

        for (Iterator<Projectile> iterator = projectiles.iterator(); iterator.hasNext(); ) {
            Projectile projectile = iterator.next();

            if (projectile.hasCollided()) {
                iterator.remove();
                continue;
            }

            double deltaTime = 1.0 / 60; // Supposons 60 FPS
            int cellWidth = (int) Math.round(canvas.getWidth() / maze.getCols());
            int cellHeight = (int) Math.round(canvas.getHeight() / maze.getRows());
            projectile.updatePosition(deltaTime, maze, cellWidth, cellHeight);

            for (Iterator<Monster> monsterIterator = monsters.iterator(); monsterIterator.hasNext(); ) {
                Monster monster = monsterIterator.next();

                if (checkProjectileMonsterCollision(projectile, monster, cellWidth, cellHeight)) {
                    monster.takeDamage(20);
                    iterator.remove();

                    if (monster.getHealth() <= 0) {
                        monsterIterator.remove();
                    }

                    break;
                }
            }

            gc.drawImage(projectile.getTexture(), projectile.getX(), projectile.getY(), 10, 10);
        }

        adjustTopBarPosition();

        if (!win && hasPlayerReachedDoor() && player.hasKey()) {
            win = true;
            playCongratulationSound();
            stopGame();
        }
    }

    private boolean checkProjectileMonsterCollision(Projectile projectile, Monster monster, int cellWidth, int cellHeight) {

        int projectileX = (int) Math.floor(projectile.getX() / cellWidth);
        int projectileY = (int) Math.floor(projectile.getY() / cellHeight);

        return projectileX == monster.getX() && projectileY == monster.getY();
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


    private void startTimer() {
        timerAnimation = new AnimationTimer() {
            @Override
            public void handle(long now) {

                double elapsedTime = (now - startTime) / 1_000_000_000.0;

                int minutes = (int) (elapsedTime / 60);
                int seconds = (int) (elapsedTime % 60);
                timeLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
            }
        };

        timerAnimation.start();
    }

    @FXML
    public void exitGame() {
        Stage stage = (Stage) exit.getScene().getWindow();
        stage.close();
    }

    public void setName(String name){
        this.playerNameLabel.setText(name);
    }

    private boolean hasPlayerReachedDoor() {
        MazeGenerator.Cell doorCell = maze.getDoorCell();
        return player.getX() == doorCell.getX() && player.getY() == doorCell.getY();
    }


    private void showLevelDialog() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jeu/roguelike2d/view/level-dialog.fxml"));
            Parent root = loader.load();
            LevelDialogController controller = loader.getController();
            controller.setController(this);
            controller.updateUI();
            Scene scene = new Scene(root);
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle(win ? "Victoire !" : "Perdu !");
            dialogStage.setScene(scene);
            dialogStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            Stage primaryStage = (Stage) canvas.getScene().getWindow();
            dialogStage.initOwner(primaryStage);
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playGunShotSound() {
        String soundPath = getClass().getResource("/com/jeu/roguelike2d/sons/gun-shot.mp3").toString();
        Media sound = new Media(soundPath);
        MediaPlayer explosionSound = new MediaPlayer(sound);
        explosionSound.play();
    }
    private void playRewardSound() {
        String soundPath = getClass().getResource("/com/jeu/roguelike2d/sons/game-bonus.mp3").toString();
        Media sound = new Media(soundPath);
        MediaPlayer explosionSound = new MediaPlayer(sound);
        explosionSound.play();
    }
    private void playTrapSound() {
        String soundPath = getClass().getResource("/com/jeu/roguelike2d/sons/ouch.mp3").toString();
        Media sound = new Media(soundPath);
        MediaPlayer explosionSound = new MediaPlayer(sound);
        explosionSound.play();
    }
    private void playCongratulationSound() {
        String soundPath = getClass().getResource("/com/jeu/roguelike2d/sons/congratulations.mp3").toString();
        Media sound = new Media(soundPath);
        MediaPlayer explosionSound = new MediaPlayer(sound);
        explosionSound.play();
    }
    private void playLooseSound() {
        String soundPath = getClass().getResource("/com/jeu/roguelike2d/sons/you-lose.mp3").toString();
        Media sound = new Media(soundPath);
        MediaPlayer explosionSound = new MediaPlayer(sound);
        explosionSound.play();
    }

    private void adjustTopBarPosition() {
        if (player.getY() <= 1) {
            topBar.setOpacity(0.1);
        } else {
            topBar.setOpacity(1);
        }
    }

    public boolean hasWin() {
        return this.win;
    }
}
