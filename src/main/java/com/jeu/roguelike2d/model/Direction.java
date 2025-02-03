package com.jeu.roguelike2d.model;

public enum Direction {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public static Direction fromDelta(int dx, int dy) {
        for (Direction direction : values()) {
            if (direction.getDx() == dx && direction.getDy() == dy) {
                return direction;
            }
        }
        return null;
    }
}