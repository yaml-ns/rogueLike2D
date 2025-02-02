package com.jeu.roguelike2d.model;

import com.jeu.roguelike2d.controller.GameController;
import javafx.scene.image.Image;

public class Player {
    private int x, y;
    private Direction direction;
    private final Image upTexture;
    private final Image downTexture;
    private final Image leftTexture;
    private final Image rightTexture;

    private static final long SHOOT_COOLDOWN = 500;
    private long lastShotTime = 0;
    private int health = 100;
    public int getHealth() { return health; }
    public boolean isAlive() { return health > 0; }
    public Player(int x, int y, Image upTexture, Image downTexture, Image leftTexture, Image rightTexture) {
        this.x = x;
        this.y = y;
        this.direction = Direction.DOWN;
        this.upTexture = upTexture;
        this.downTexture = downTexture;
        this.leftTexture = leftTexture;
        this.rightTexture = rightTexture;
    }


    public Projectile shoot(Direction direction) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < SHOOT_COOLDOWN) {
            return null;
        }

        lastShotTime = currentTime;

        double dirX = 0, dirY = 0;
        switch (direction) {
            case UP: dirY = -1; break;
            case DOWN: dirY = 1; break;
            case LEFT: dirX = -1; break;
            case RIGHT: dirX = 1; break;
        }

        return new Projectile(
                x + GameController.CELL_SIZE / 2,
                y + GameController.CELL_SIZE / 2,
                dirX, dirY, true
        );
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Image getCurrentTexture() {
        switch (direction) {
            case UP:
                return upTexture;
            case DOWN:
                return downTexture;
            case LEFT:
                return leftTexture;
            case RIGHT:
                return rightTexture;
            default:
                return downTexture;
        }
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public Projectile shoot() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < SHOOT_COOLDOWN) {
            return null;
        }

        lastShotTime = currentTime;

        // Position de départ au centre du joueur
        double startX = x * GameController.CELL_SIZE + GameController.CELL_SIZE / 2;
        double startY = y * GameController.CELL_SIZE + GameController.CELL_SIZE / 2;

        // Direction basée sur l'orientation du joueur
        double dirX = 0, dirY = 0;
        switch (direction) {
            case UP:    dirY = -1; break;
            case DOWN:  dirY = 1;  break;
            case LEFT:  dirX = -1; break;
            case RIGHT: dirX = 1;  break;
        }

        System.out.println("Tir dans la direction: " + direction + " (dx=" + dirX + ", dy=" + dirY + ")");
        return new Projectile(startX, startY, dirX, dirY, true);
    }
}


