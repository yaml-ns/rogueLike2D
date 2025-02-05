package com.jeu.roguelike2d.model;
import javafx.scene.image.Image;

import java.util.Objects;

public class Player extends Character {
    private final String weapon;
    private int energy = 100;
    private final Image[] textures;
    private Image currentTexture;

    private boolean hasKey = false;

    public Player(int health, int damage, String name, Image currentTexture) {
        super(0, 0, health, damage, name, currentTexture);
        this.currentTexture = currentTexture;
        this.weapon = "Fusil";
        this.textures = new Image[4];
        this.textures[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-up.png")));    // UP
        this.textures[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-front.png"))); // DOWN
        this.textures[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-left.png"))); // LEFT
        this.textures[3] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/jeu/roguelike2d/images/player-right.png"))); // RIGHT

    }

    public void setDirectionTexture(Direction direction) {
        switch (direction) {
            case UP -> currentTexture = textures[0];
            case DOWN -> currentTexture = textures[1];
            case LEFT -> currentTexture = textures[2];
            case RIGHT -> currentTexture = textures[3];
        }
    }

    @Override
    public boolean move(int dx, int dy, MazeGenerator maze) {
        int newX = x + dx;
        int newY = y + dy;

        if (maze.canMove(x, y, dx, dy)) {
            x = newX;
            y = newY;

            Direction direction = Direction.fromDelta(dx, dy);
            setDirectionTexture(direction);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Image getTexture() {
        return currentTexture;
    }

    public String getWeapon() {
        return weapon;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public boolean hasKey(){
        return hasKey;
    }
    public void setHasKey(boolean b) {
        this.hasKey = b;
    }
}