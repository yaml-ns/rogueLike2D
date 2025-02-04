package com.jeu.roguelike2d.model;

import com.jeu.roguelike2d.controller.GameController;
import javafx.scene.image.Image;


public class Dragon extends Monster {
    private GameController controller;

    public Dragon(int x, int y, Image texture, int cellWidth, int cellHeight) {
        super(x, y, 100, 25, "Dragon", 10, texture, cellWidth, cellHeight);

    }

    public void setController(GameController controller) {
        this.controller = controller;
    }






}
