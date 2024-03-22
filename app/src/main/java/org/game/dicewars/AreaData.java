package org.game.dicewars;

public class AreaData {
    public int size = 0;
    public int centerCellIx = 0; // center cell
    public int playerIx = 0; // belongs to the army
    public int dices = 0; // number of dice

    // parameters to determine the center
    public int left = 0;
    public int right = 0;
    public int top = 0;
    public int bottom = 0;
    public int cx = 0; // left,right middle ground
    public int cy = 0; // top,bottom middle ground
    public int lengthMin = 0;

    // For building surrounding lines
    public int linesCount;
    public int[] lineDrawingCellIx = new int[100]; // cell
    public int[] lineDrawingDirection = new int[100]; // direction
    // 32 neighbor cell flags
    public boolean[] neighbors;

    public AreaData(int neighborCells) {
        neighbors = new boolean[neighborCells];
    }
}