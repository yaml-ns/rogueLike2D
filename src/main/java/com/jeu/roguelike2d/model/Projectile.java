package com.jeu.roguelike2d.model;

import javafx.scene.image.Image;

public class Projectile {
    private double x, y;
    private final int dx, dy;
    private final double speed;
    private final Image texture;
    private boolean hasCollided = false; // Indicateur de collision

    public Projectile(double startX, double startY, int dx, int dy, Image texture) {
        this.x = startX;
        this.y = startY;
        this.dx = dx;
        this.dy = dy;
        this.speed = 600;
        this.texture = texture;
    }

    public void updatePosition(double deltaTime, MazeGenerator maze, int cellWidth, int cellHeight) {
        if (hasCollided) {
            return; // Ne rien faire si le projectile a déjà touché un mur
        }

        double nextX = x + dx * speed * deltaTime;
        double nextY = y + dy * speed * deltaTime;

        // Calculer les positions de départ et d'arrivée en coordonnées de grille
        int startX = (int) Math.floor(x / cellWidth);
        int startY = (int) Math.floor(y / cellHeight);
        int endX = (int) Math.floor(nextX / cellWidth);
        int endY = (int) Math.floor(nextY / cellHeight);

        // Vérifier toutes les cellules traversées et les murs entre elles
        if (!isPathClear(startX, startY, endX, endY, maze, cellWidth, cellHeight)) {
            // Collision détectée : marquer le projectile comme touché
            hasCollided = true;
            return;
        }

        // Mettre à jour la position du projectile
        x = nextX;
        y = nextY;
    }

    private boolean isPathClear(int startX, int startY, int endX, int endY, MazeGenerator maze, int cellWidth, int cellHeight) {
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);
        int sx = (startX < endX) ? 1 : -1;
        int sy = (startY < endY) ? 1 : -1;
        int err = dx - dy;

        int currentX = startX;
        int currentY = startY;

        while (true) {
            // Vérifier si la cellule actuelle est libre
            if (!maze.isCellFree(currentX, currentY)) {
                return false; // Collision détectée
            }

            // Vérifier les murs entre les cellules
            if (currentX != startX || currentY != startY) {
                if (currentX != startX && maze.hasWallBetween(currentX, currentY, currentX - sx, currentY)) {
                    return false; // Mur horizontal détecté
                }
                if (currentY != startY && maze.hasWallBetween(currentX, currentY, currentX, currentY - sy)) {
                    return false; // Mur vertical détecté
                }
            }

            if (currentX == endX && currentY == endY) {
                break; // Arrivé à destination
            }

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                currentX += sx;
            }
            if (e2 < dx) {
                err += dx;
                currentY += sy;
            }
        }
        return true; // Aucune collision détectée
    }

    public boolean hasCollided() {
        return hasCollided; // Retourne true si le projectile a touché un mur
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Image getTexture() {
        return texture;
    }
}