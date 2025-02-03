package com.jeu.roguelike2d.model;

public interface Movable {
    boolean move(int dx, int dy, MazeGenerator maze);
}