package com.jeu.roguelike2d.model;

import javafx.scene.image.Image;

public class Reward extends StaticObject {
    private int healthBonus; // Bonus de santé

    public Reward(int x, int y, String name,Image texture, int healthBonus) {
        super(x, y, name, texture);
        this.healthBonus = healthBonus;
    }

    @Override
    public void onContact(Character entity) {
        if (entity instanceof Player player) {
            player.setHealth(player.getHealth()+healthBonus);
            System.out.println("Player collected a reward! Health increased by " + healthBonus);
        }
    }
}