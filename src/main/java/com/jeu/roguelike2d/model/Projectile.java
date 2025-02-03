package com.jeu.roguelike2d.model;

import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;

public class Projectile {
    // Position du projectile
    private double x;
    private double y;

    // Direction normalisée du projectile
    private final double directionX;
    private final double directionY;

    // Paramètres du projectile
    private static final double SPEED = 8.0;
    private static final int SIZE = 8;
    private static final int DAMAGE = 25;

    // État du projectile
    private boolean active = true;
    private final boolean isPlayerProjectile;
    private final Color color;


    public Projectile(double startX, double startY, double directionX, double directionY, boolean isPlayerProjectile) {
        this.x = startX;
        this.y = startY;

        // Normalisation du vecteur de direction
        double length = Math.sqrt(directionX * directionX + directionY * directionY);
        if (length != 0) {
            this.directionX = directionX / length;
            this.directionY = directionY / length;
        } else {
            this.directionX = 0;
            this.directionY = 0;
        }

        this.isPlayerProjectile = isPlayerProjectile;
        this.color = isPlayerProjectile ? Color.YELLOW : Color.RED;
    }

    public void update() {
        if (!active) return;

        // Mise à jour de la position
        x += directionX * SPEED;
        y += directionY * SPEED;
    }

    public void draw(GraphicsContext gc) {
        if (!active) return;

        gc.setFill(color);
        gc.fillOval(x - SIZE/2, y - SIZE/2, SIZE, SIZE);
    }

    public boolean collidesWith(double targetX, double targetY, double targetSize) {
        if (!active) return false;

        double distance = Math.sqrt(
                Math.pow(x - targetX, 2) +
                        Math.pow(y - targetY, 2)
        );

        return distance < (targetSize + SIZE/2);
    }

    public void setPosition(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }

    public boolean collidesWithWall(int gridX, int gridY, int cellSize) {
        if (!active) return false;

        // Vérifier si le projectile est à l'intérieur de la cellule
        double leftWall = gridX * cellSize;
        double rightWall = (gridX + 1) * cellSize;
        double topWall = gridY * cellSize;
        double bottomWall = (gridY + 1) * cellSize;

        return x >= leftWall && x <= rightWall &&
                y >= topWall && y <= bottomWall;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getDamage() { return DAMAGE; }
    public boolean isActive() { return active; }
    public boolean isPlayerProjectile() { return isPlayerProjectile; }

    // Setters
    public void deactivate() { active = false; }

    public static int getProjectileSize() { return SIZE; }

    public double getDirectionX() { return directionX; }
    public double getDirectionY() { return directionY; }
    public static double getSpeed() { return SPEED; }
    @Override
    public String toString() {
        return String.format(
                "Projectile[x=%.2f, y=%.2f, dir=(%.2f,%.2f), active=%b, player=%b]",
                x, y, directionX, directionY, active, isPlayerProjectile
        );
    }
}