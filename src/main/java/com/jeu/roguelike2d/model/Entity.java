package com.jeu.roguelike2d.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.Image;

public abstract class Entity {
    protected int x, y;
    protected final DoubleProperty realX = new SimpleDoubleProperty();
    protected final DoubleProperty realY = new SimpleDoubleProperty();
    protected String name;
    protected Image texture;


    public Entity(int x, int y, String name, Image texture) {
        this.x = x;
        this.y = y;
        this.realX.set(0);
        this.realY.set(0);
        this.name = name;
        this.texture = texture;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getRealX() {
        return realX.get();
    }

    public double getRealY() {
        return realY.get();
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setRealX(double realX) {
        this.realX.set(realX);
    }

    public void setRealY(double realY) {
        this.realY.set(realY);
    }

    public DoubleProperty realXProperty() {
        return realX;
    }

    public DoubleProperty realYProperty() {
        return realY;
    }


    public String getName() {
        return name;
    }

    public Image getTexture() {
        return texture;
    }
}