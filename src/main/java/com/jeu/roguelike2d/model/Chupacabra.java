package com.jeu.roguelike2d.model;

import com.jeu.roguelike2d.controller.GameController;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class Chupacabra extends Monster {

    private GameController controller;
    public Chupacabra(int x, int y, Image texture,int cellWidth, int cellHeight) {
        super(x, y, 40, 15, "Chupacabra", 3, texture, cellWidth, cellHeight);
        this.setSpeed(80);

    }



    public void setController(GameController gameController) {
    this.controller = gameController;
    }
}