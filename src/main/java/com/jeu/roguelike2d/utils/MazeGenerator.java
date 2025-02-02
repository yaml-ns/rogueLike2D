package com.jeu.roguelike2d.utils;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class MazeGenerator {
    private final int cols, rows;
    private final int cellSize;
    private final Cell[][] grid;
    private final Stack<Cell> stack = new Stack<>();
    private Cell current;
    private Cell doorCell;
    private final Image wallTexture;
    private final Image floorTexture;
    private final Image doorTexture;

    public MazeGenerator(int cols, int rows, int cellSize, Image wallTexture, Image floorTexture, Image doorTexture) {
        this.cols = cols;
        this.rows = rows;
        this.cellSize = cellSize;
        this.wallTexture = wallTexture;
        this.floorTexture = floorTexture;
        this.doorTexture = doorTexture;
        grid = new Cell[cols][rows];

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                grid[x][y] = new Cell(x, y);
            }
        }

        if (cols > 0 && rows > 0) {
            current = grid[0][0];
            current.visited = true;
            stack.push(current);
        }
    }

    public boolean canMove(int x, int y, int dx, int dy) {
        int newX = x + dx;
        int newY = y + dy;
        if (newX < 0 || newX >= cols || newY < 0 || newY >= rows) return false;
        return !(dx == 1 && grid[x][y].right) && !(dx == -1 && grid[x][y].left) &&
                !(dy == 1 && grid[x][y].bottom) && !(dy == -1 && grid[x][y].top);
    }

    public boolean generateStep() {
        if (stack.isEmpty()) {
            placeDoor();
            return false; // Fin de la génération
        }

        // Récupérer une cellule non visitée
        Cell next = current.getRandomNeighbor(grid, cols, rows);
        if (next != null) {
            next.visited = true;
            stack.push(current);
            removeWall(current, next);
            current = next;
        } else {
            current = stack.pop(); // Backtracking
        }
        return true;
    }

    private void placeDoor() {
        // Place the door at the bottom-right cell of the maze
        doorCell = grid[cols - 1][rows - 1];
    }

    private void removeWall(Cell a, Cell b) {
        int dx = b.x - a.x;
        int dy = b.y - a.y;
        if (dx == 1) { a.right = false; b.left = false; }
        if (dx == -1) { a.left = false; b.right = false; }
        if (dy == 1) { a.bottom = false; b.top = false; }
        if (dy == -1) { a.top = false; b.bottom = false; }
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(Color.BLANCHEDALMOND);
        gc.fillRect(0, 0, cols * cellSize, rows * cellSize);

        for (Cell[] row : grid) {
            for (Cell cell : row) {
                cell.draw(gc, cellSize, wallTexture, floorTexture);
            }
        }

        if (doorCell != null) {
            gc.drawImage(doorTexture, doorCell.getX() * cellSize, doorCell.getY() * cellSize, cellSize, cellSize);
        }
    }

    public Cell getDoorCell() {
        return doorCell;
    }

    public class Cell {
        int x, y;
        boolean visited = false;
        boolean top = true, right = true, bottom = true, left = true;

        Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        void draw(GraphicsContext gc, int size, Image wallTexture, Image floorTexture) {
            int px = x * size;
            int py = y * size;

            double wallThickness = size * 0.3;
            double overlap = wallThickness;

            gc.drawImage(floorTexture, px, py, size, size);

            gc.setFill(new ImagePattern(wallTexture, 0, 0, wallTexture.getWidth(), wallTexture.getHeight(), false));

            if (top) gc.drawImage(wallTexture, px - overlap, py - overlap, size + wallThickness, wallThickness);
            if (right) gc.drawImage(wallTexture, px + size - overlap, py - overlap, wallThickness, size + wallThickness);
            if (bottom) gc.drawImage(wallTexture, px - overlap, py + size - overlap, size + wallThickness, wallThickness);
            if (left) gc.drawImage(wallTexture, px - overlap, py - overlap, wallThickness, size + wallThickness);
        }

        Cell getRandomNeighbor(Cell[][] grid, int cols, int rows) {
            ArrayList<Cell> neighbors = new ArrayList<>();
            if (x > 0 && !grid[x - 1][y].visited) neighbors.add(grid[x - 1][y]);
            if (x < cols - 1 && !grid[x + 1][y].visited) neighbors.add(grid[x + 1][y]);
            if (y > 0 && !grid[x][y - 1].visited) neighbors.add(grid[x][y - 1]);
            if (y < rows - 1 && !grid[x][y + 1].visited) neighbors.add(grid[x][y + 1]);

            if (neighbors.isEmpty()) return null;
            Collections.shuffle(neighbors);
            return neighbors.get(0);
        }
    }
}
