package com.jeu.roguelike2d.model;

import javafx.scene.image.Image;

public class Player {
    private int x, y;
    private Direction direction;
    private final Image upTexture;
    private final Image downTexture;
    private final Image leftTexture;
    private final Image rightTexture;

    public Player(int x, int y, Image upTexture, Image downTexture, Image leftTexture, Image rightTexture) {
        this.x = x;
        this.y = y;
        this.direction = Direction.DOWN;
        this.upTexture = upTexture;
        this.downTexture = downTexture;
        this.leftTexture = leftTexture;
        this.rightTexture = rightTexture;
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
}
