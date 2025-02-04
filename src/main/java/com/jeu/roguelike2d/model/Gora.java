package com.jeu.roguelike2d.model;

import com.jeu.roguelike2d.controller.GameController;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class Gora extends Monster {
    private Entity target;

    public void setController(GameController controller) {
        this.controller = controller;
    }

    private GameController controller;

    public Gora(int x, int y, Image texture, Entity target, int cellWidth, int cellHeight) {
        super(x, y, 30, 20, "Gora", 0, texture,cellWidth, cellHeight );
        this.target = target;
        this.setSpeed(8);
    }


}