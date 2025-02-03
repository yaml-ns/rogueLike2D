package com.jeu.roguelike2d.model;

import com.jeu.roguelike2d.controller.GameController;
import javafx.scene.image.Image;

public class Chupacabra extends Monster {

    private GameController controller;
    private Entity target;
    public Chupacabra(int x, int y, Image texture,Entity target) {
        super(x, y, 40, 15, "Chupacabra", 3, texture);
        this.target = target;
    }

    @Override
    public void autoMove(MazeGenerator maze) {
        // 20 % de chance de ne pas bouger
        if (Math.random() < 0.2) {
            return;
        }

        // Générer une direction aléatoire
        int[] directions = { -1, 0, 1 };
        int dx = directions[(int) (Math.random() * directions.length)];
        int dy = directions[(int) (Math.random() * directions.length)];

        // Vérifier si le déplacement est valide (pas un mur)
        if (maze.canMove(getX(), getY(), dx, dy)) {
            move(dx, dy, maze); // Mettre à jour les coordonnées en cellules
            controller.animateMonsterMovement(this, dx, dy); // Appeler l'animation via le contrôleur
        }
    }


    public void setController(GameController gameController) {
    this.controller = gameController;
    }
}