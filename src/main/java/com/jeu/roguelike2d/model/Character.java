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
        int newX = x + dx;
        int newY = y + dy;

        if (maze.canMove(x, y, dx, dy)) {
            x = newX;
            y = newY;
            return true;
        } else {
            System.out.println("Déplacement impossible !");
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
}
