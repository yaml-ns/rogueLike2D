package com.jeu.roguelike2d.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class MazeGenerator {

    private final int cols, rows;
    private final int cellWidth, cellHeight;
    private final Cell[][] grid;
    private final Stack<Cell> stack = new Stack<>();
    private Cell current;
    private Cell doorCell;
    private final Image wallTexture;
    private final Image floorTexture;
    private final Image doorTexture;

    public MazeGenerator(int cols, int rows, int cellSize, Image wallTexture, Image floorTexture, Image doorTexture) {
        this(cols, rows, cellSize, cellSize, wallTexture, floorTexture, doorTexture);
    }

    public MazeGenerator(int cols, int rows, int cellWidth, int cellHeight, Image wallTexture, Image floorTexture, Image doorTexture) {
        this.cols = cols;
        this.rows = rows;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
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

    public int getWidth() {
        return cols * cellWidth;
    }

    public int getHeight() {
        return rows * cellHeight;
    }

    public boolean canMove(int x, int y, int dx, int dy) {
        int newX = x + dx;
        int newY = y + dy;

        if (newX < 0 || newX >= cols || newY < 0 || newY >= rows) {
            return false;
        }

        Cell currentCell = grid[x][y];

        if (dx == 1 && currentCell.right) {
            return false;
        }
        if (dx == -1 && currentCell.left) {
            return false;
        }
        if (dy == 1 && currentCell.bottom) {
            return false;
        }
        if (dy == -1 && currentCell.top) {
            return false;
        }

        return true;
    }
    public boolean generateStep() {
        if (stack.isEmpty()) {
            placeDoor();
            return false;
        }
        Cell next = current.getRandomNeighbor(grid, cols, rows);
        if (next != null) {
            next.visited = true;
            stack.push(current);
            removeWall(current, next);
            current = next;
        } else {
            current = stack.pop();
        }
        return true;
    }

    private void placeDoor() {
        doorCell = grid[cols - 1][rows - 1];
    }

    public boolean isCellFree(int x, int y) {
        if (x < 0 || x >= cols || y < 0 || y >= rows) {
            return false;
        }

        Cell cell = grid[x][y];
        return !(cell.top && cell.right && cell.bottom && cell.left);
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
        gc.fillRect(0, 0, cols * cellWidth, rows * cellHeight);
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                cell.draw(gc, cellWidth, cellHeight, wallTexture, floorTexture);
            }
        }
        if (doorCell != null) {
            gc.drawImage(doorTexture, doorCell.getX() * cellWidth, doorCell.getY() * cellHeight, cellWidth, cellHeight);
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

        void draw(GraphicsContext gc, int width, int height, Image wallTexture, Image floorTexture) {
            int px = x * width;
            int py = y * height;

            if (floorTexture != null && !floorTexture.isError()) {
                gc.drawImage(floorTexture, px, py, width, height);
            } else {
                gc.setFill(Color.LIGHTGRAY);
                gc.fillRect(px, py, width, height);
            }

            double wallThickness = Math.min(width, height) * 0.15;
            double overlap = wallThickness;

            if (wallTexture != null && !wallTexture.isError()) {
                if (top) gc.drawImage(wallTexture, px - overlap, py - overlap, width + wallThickness, wallThickness);
                if (right) gc.drawImage(wallTexture, px + width - overlap, py - overlap, wallThickness, height + wallThickness);
                if (bottom) gc.drawImage(wallTexture, px - overlap, py + height - overlap, width + wallThickness, wallThickness);
                if (left) gc.drawImage(wallTexture, px - overlap, py - overlap, wallThickness, height + wallThickness);
            } else {
                gc.setFill(Color.BLACK);
                if (top) gc.fillRect(px - overlap, py - overlap, width + wallThickness, wallThickness);
                if (right) gc.fillRect(px + width - overlap, py - overlap, wallThickness, height + wallThickness);
                if (bottom) gc.fillRect(px - overlap, py + height - overlap, width + wallThickness, wallThickness);
                if (left) gc.fillRect(px - overlap, py - overlap, wallThickness, height + wallThickness);
            }
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

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public void printMaze() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                System.out.print(grid[x][y].top ? "+---" : "+   ");
            }
            for (int x = 0; x < cols; x++) {
                System.out.print(grid[x][y].left ? "|   " : "    ");
            }
            System.out.println("|");
        }

        for (int x = 0; x < cols; x++) {
            System.out.print("+---");
        }
        System.out.println("+");
    }

}
