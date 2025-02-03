package com.jeu.roguelike2d.model;

import javafx.scene.image.Image;

public abstract class Monster extends Character {
    protected int resistance;

    public Monster(int x, int y, int health, int damage, String name, int resistance, Image texture) {
        super(x, y, health, damage, name, texture);
        this.resistance = resistance;
    }

    @Override
    public void takeDamage(int damage) {
        int effectiveDamage = Math.max(0, damage - resistance);
        super.takeDamage(effectiveDamage);
    }

    public abstract void autoMove(MazeGenerator maze);
}