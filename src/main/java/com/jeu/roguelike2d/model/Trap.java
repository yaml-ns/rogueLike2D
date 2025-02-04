package com.jeu.roguelike2d.model;

import javafx.scene.image.Image;

public class Trap extends StaticObject {
    private int damage; // Dégâts infligés

    public Trap(int x, int y, String name, Image texture, int damage) {
        super(x, y, name, texture);
        this.damage = damage;
    }

    @Override
    public void onContact(Character entity) {
        if (entity instanceof Player){
            entity.takeDamage(damage);
            System.out.println(entity.getName() + " triggered a trap! Took " + damage + " damage.");
        }
    }
}