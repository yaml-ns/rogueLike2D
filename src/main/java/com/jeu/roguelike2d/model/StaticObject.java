package com.jeu.roguelike2d.model;

import javafx.scene.image.Image;

public abstract class StaticObject extends Entity{
    protected int x, y;
    protected Image texture;

    public StaticObject(int x, int y, String name, Image texture) {
        super(x,y,name,texture);
        this.x = x;
        this.y = y;
        this.texture = texture;
    }

    public abstract void onContact(Character entity);
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Image getTexture() {
        return texture;
    }
}