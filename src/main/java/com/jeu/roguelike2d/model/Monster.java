package com.jeu.roguelike2d.model;

import javafx.scene.image.Image;
import java.util.Random;

public abstract class Monster extends Character {
    private final int cellWidth;
    private final int cellHeight;
    protected int resistance;

    private double currentX, currentY;
    private double targetX, targetY;
    private int directionX = 0, directionY = 0;
    private int speed = 10;
    private long lastMoveTime = 0;
    private static final long MOVE_DELAY = 150_000_000;
    private Random random = new Random();
    private int cellSize;

    private boolean isAlive = true;

    public Monster(int x, int y, int health, int damage, String name, int resistance, Image texture, int cellWidth, int cellHeight) {
        super(x, y, health, damage, name, texture);
        this.resistance = resistance;

        this.currentX = x * cellWidth;
        this.currentY = y * cellHeight;
        this.targetX = currentX;
        this.targetY = currentY;

        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
    }

    @Override
    public void takeDamage(int damage) {
        int effectiveDamage = Math.max(0, damage - resistance);
        super.takeDamage(effectiveDamage);
    }

    public void monsterMove(MazeGenerator maze) {
        if (!isAlive()) {
            return;
        }

        if (currentX == targetX && currentY == targetY) {

            chooseNewDirection(maze);
            if (directionX != 0 || directionY != 0) {

                targetX = (getX() + directionX) * cellWidth;
                targetY = (getY() + directionY) * cellHeight;

            }
        }

        if (currentX != targetX || currentY != targetY) {

            if (currentX < targetX) {
                currentX = Math.min(currentX + speed, targetX);
            } else if (currentX > targetX) {
                currentX = Math.max(currentX - speed, targetX);
            }

            if (currentY < targetY) {
                currentY = Math.min(currentY + speed, targetY);
            } else if (currentY > targetY) {
                currentY = Math.max(currentY - speed, targetY);
            }

        }

        setX((int) (currentX / cellWidth));
        setY((int) (currentY / cellHeight));

    }

    private void chooseNewDirection(MazeGenerator maze) {

        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int i = directions.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] temp = directions[i];
            directions[i] = directions[j];
            directions[j] = temp;
        }

        for (int[] dir : directions) {
            int dx = dir[0];
            int dy = dir[1];

            if (maze.canMove(getX(), getY(), dx, dy)) {
                directionX = dx;
                directionY = dy;
                return;
            }
        }

        directionX = 0;
        directionY = 0;

    }


      public void autoMove(MazeGenerator maze) {

          long now = System.nanoTime();
         if (now - lastMoveTime < MOVE_DELAY) {
              return;
          }

         lastMoveTime = now;
         monsterMove(maze);
    }

    public double getCurrentX() {
        return currentX;
    }

    public double getCurrentY() {
        return currentY;
    }
    @Override
    public boolean isAlive() {
        return isAlive;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}