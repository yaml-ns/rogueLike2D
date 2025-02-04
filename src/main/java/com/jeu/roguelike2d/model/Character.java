package com.jeu.roguelike2d.model;

import javafx.scene.image.Image;

public abstract class Character extends Entity implements Movable {
    protected int health;
    protected int damage;

    public Character(int x, int y, int health, int damage, String name, Image texture) {
        super(x, y, name, texture);
        this.health = health;
        this.damage = damage;
    }

    @Override
    public boolean move(int dx, int dy, MazeGenerator maze) {
        int newX = getX() + dx;
        int newY = getY() + dy;

        if (maze.canMove(getX(), getY(), dx, dy)) {
            setX(newX);
            setY(newY);
            return true;
        } else {
            return false;
        }
    }
    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            System.out.println(name + " est mort !");
        } else {
            System.out.println(name + " a maintenant " + health + " points de vie.");
        }
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void attack(Entity target) {
        if (target instanceof Character) {
            ((Character) target).takeDamage(damage);
        }
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
}
