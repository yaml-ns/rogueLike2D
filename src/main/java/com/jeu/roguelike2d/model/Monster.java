package com.jeu.roguelike2d.model;

import com.jeu.roguelike2d.controller.GameController;
import com.jeu.roguelike2d.utils.MazeGenerator;
import javafx.scene.image.Image;
import java.util.Random;

public class Monster {
    private int x, y;
    private double currentX, currentY;
    private double targetX, targetY;
    private int health;
    private Image texture;
    private int directionX, directionY;
    private static final double SPEED = 2.0;
    private boolean isMoving = false;
    private final Random random = new Random();
    private static final long SHOOT_COOLDOWN = 1500; // 1.5s between shots
    private long lastShotTime = 0;

    public Monster(int x, int y, Image texture, int health, MazeGenerator maze) {
        this.x = x;
        this.y = y;
        this.currentX = x * GameController.CELL_SIZE;
        this.currentY = y * GameController.CELL_SIZE;
        this.targetX = currentX;
        this.targetY = currentY;
        this.health = health;
        this.texture = texture;
        chooseNewDirection(maze);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public double getCurrentX() { return currentX; }
    public double getCurrentY() { return currentY; }
    public int getHealth() { return health; }
    public Image getTexture() { return texture; }
    public boolean isAlive() { return health > 0; }

    public void takeDamage(int damage) {
        health -= damage;
    }

    public void move(MazeGenerator maze, int cellSize) {
        if (!isAlive()) return;

        // Si on a atteint la position cible ou qu'on ne bouge pas encore
        if (currentX == targetX && currentY == targetY) {
            // Choisir une nouvelle direction si possible
            chooseNewDirection(maze);
            if (directionX != 0 || directionY != 0) {
                // Calculer la nouvelle cible
                targetX = currentX + (directionX * cellSize);
                targetY = currentY + (directionY * cellSize);
            }
        }

        // Déplacer vers la cible
        if (currentX != targetX || currentY != targetY) {
            // Déplacement horizontal
            if (currentX < targetX) {
                currentX = Math.min(currentX + SPEED, targetX);
            } else if (currentX > targetX) {
                currentX = Math.max(currentX - SPEED, targetX);
            }

            // Déplacement vertical
            if (currentY < targetY) {
                currentY = Math.min(currentY + SPEED, targetY);
            } else if (currentY > targetY) {
                currentY = Math.max(currentY - SPEED, targetY);
            }
        }

        // Mettre à jour les coordonnées de la grille
        x = (int) (currentX / cellSize);
        y = (int) (currentY / cellSize);
    }

    private void chooseNewDirection(MazeGenerator maze) {
        // Directions possibles : droite, gauche, bas, haut
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        // Mélanger les directions
        for (int i = directions.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] temp = directions[i];
            directions[i] = directions[j];
            directions[j] = temp;
        }

        // Essayer chaque direction
        for (int[] dir : directions) {
            if (maze.canMove(x, y, dir[0], dir[1])) {
                directionX = dir[0];
                directionY = dir[1];
                return;
            }
        }

        // Si aucune direction n'est valide
        directionX = 0;
        directionY = 0;
    }



    public Projectile shoot(double playerX, double playerY) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < SHOOT_COOLDOWN) {
            return null;
        }

        lastShotTime = currentTime;

        double dirX = playerX - currentX;
        double dirY = playerY - currentY;

        return new Projectile(
                currentX + (double) GameController.CELL_SIZE / 2,
                currentY + (double) GameController.CELL_SIZE / 2,
                dirX, dirY, false
        );
    }
}