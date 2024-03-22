package org.game.dicewars;

import org.game.dicewars.ai.AI;
import org.game.dicewars.ai.AIDefault;
import org.game.dicewars.ai.AIDefensive;
import org.game.dicewars.ai.AIExample;

public class Game {
    public final AI[] ai;

    // cell data
    public final int XMAX;
    public final int YMAX;

    final int CELL_MAX;
    // history list
    // public final List<HistoryData> history = new ArrayList<>();

    // max number of player areas
    public final int AREA_MAX;

    public int[] allBoardCells;
    NeighborData[] neighborCell;
    public AreaData[] allAreaData;
    int[] cellIndex;
    boolean[] neighborCellFlag;
    int[] neighborFlag;
    int[] chk;
    int[] areaTotalCount;
    public int maxPlayerCount;
    public int userPlayerIx; // the human player id
    int placeDicesAverage;
    public int[] playersOrder;
    public int currentPlayer;
    public int attackAreaFrom;
    public int attackAreaTo;
    int attackSuccess;
    public PlayerData[] playersData;
    public final int STOCK_MAX;
    int[] listCellIndexFrom;
    int[] listCellIndexTo;
    // int[] historyPlayer;
    // int[] historyDice;

    // 28, 32
    public Game() {
        XMAX = 28;
        YMAX = 32;
        CELL_MAX = XMAX * YMAX;

        ai = new AI[]{
                // user
                new AIDefensive(),
                // p1
                new AIExample(),
                // p2
                new AIDefensive(),
                // p3
                new AIDefensive(),
                // p4
                new AIDefault(),
                // p5
                new AIDefault(),
                // p6
                new AIDefault(),
                // p7
                new AIDefault()};

        allBoardCells = new int[CELL_MAX];
        // arrangement with adjacent cells
        neighborCell = new NeighborData[CELL_MAX];
        for (int i = 0; i < CELL_MAX; i++) {
            neighborCell[i] = new NeighborData();
            for (int j = 0; j < 6; j++) {
                neighborCell[i].direction[j] = getNeighborCell(i, j);
            }
        }
        // area data
        AREA_MAX = 32; // maximum number of areas
        allAreaData = new AreaData[AREA_MAX];
        for (int i = 0; i < 32; i++) {
            allAreaData[i] = new AreaData(AREA_MAX);
        }
        // used for map creation
        cellIndex = new int[CELL_MAX]; // area serial number
        for (int i = 0; i < CELL_MAX; i++) {
            cellIndex[i] = i;
        }
        neighborCellFlag = new boolean[CELL_MAX]; // adjacent cell

        neighborFlag = new int[CELL_MAX]; // peripheral cell to use for penetration
        chk = new int[AREA_MAX]; // for area drawing lines
        areaTotalCount = new int[AREA_MAX]; // used in adjacent area number

        // game data
        maxPlayerCount = 7; // number of players
        userPlayerIx = 0; // user player
        placeDicesAverage = 3; // average number of placement dice
        playersOrder = new int[]{0, 1, 2, 3, 4, 5, 6, 7}; // player order
        currentPlayer = 0; // player = jun[ban]; (the current player is player = jun[ban];)
        attackAreaFrom = 0; // attack source
        attackAreaTo = 0; // attack destination
        attackSuccess = 0; // 0. attack failure, 1. attack success

        // player data
        playersData = new PlayerData[8];
        STOCK_MAX = 64; // maximum number of stocks
        // ai thinking
        listCellIndexFrom = new int[AREA_MAX * AREA_MAX];
        listCellIndexTo = new int[AREA_MAX * AREA_MAX];
        // history
        // history.clear();
        // initial placement
        // historyPlayer = new int[AREA_MAX];
        // historyDice = new int[AREA_MAX];
    }

    // Starting the game (making maps, setting pmax, user etc.)
    public void start() {
        // reset player order
        for (int i = 0; i < 8; i++) {
            playersOrder[i] = i;
        }
        // shuffle the player order
        for (int i = 0; i < maxPlayerCount; i++) {
            int r = (int) (Math.random() * maxPlayerCount);
            int tmp = playersOrder[i];
            playersOrder[i] = playersOrder[r];
            playersOrder[r] = tmp;
        }
        currentPlayer = 0;
        // create player data
        for (int i = 0; i < 8; i++) {
            playersData[i] = new PlayerData();
        }
        // calculate each player area count
        for (int i = 0; i < 8; i++) {
            calculateTotalAreaCount(i);
        }
        calculatePlayersAreas();
        // history
        // history.clear();// his_c = 0;
        // for (int i = 0; i < AREA_MAX; i++) {
        // historyPlayer[i] = allAreaData[i].playerIx;
        // historyDice[i] = allAreaData[i].dices;
        // }
    }

    public void calculatePlayersAreas() {
        for (int i = 0; i < maxPlayerCount; i++) {
            playersData[i].areaCount = 0;
        }
        for (int i = 0; i < AREA_MAX; i++) {
            int playerIx = allAreaData[i].playerIx;
            if (playerIx >= 0) {
                playersData[playerIx].areaCount++;
            }
        }
    }

    // maximum number of adjacent areas
    public void calculateTotalAreaCount(int playerIx) {
        playersData[playerIx].areaTotalCount = 0;

        for (int i = 0; i < AREA_MAX; i++) {
            chk[i] = i;
        }
        while (true) {
            boolean flag = false;
            for (int i = 1; i < AREA_MAX; i++) {
                if (allAreaData[i].size == 0) {
                    continue;
                }
                if (allAreaData[i].playerIx != playerIx) {
                    continue;
                }
                for (int j = 1; j < AREA_MAX; j++) {
                    if (allAreaData[j].size == 0) {
                        continue;
                    }
                    if (allAreaData[j].playerIx != playerIx) {
                        continue;
                    }
                    if (!allAreaData[i].neighbors[j]) {
                        continue;
                    }
                    if (chk[j] == chk[i]) {
                        continue;
                    }
                    if (chk[i] > chk[j]) {
                        chk[i] = chk[j];
                    } else {
                        chk[j] = chk[i];
                    }
                    flag = true;
                    break;
                }
                if (flag) {
                    break;
                }
            }
            if (!flag) {
                break;
            }
        }
        for (int i = 0; i < AREA_MAX; i++) {
            areaTotalCount[i] = 0;
        }
        for (int i = 1; i < AREA_MAX; i++) {
            if (allAreaData[i].size == 0) {
                continue;
            }
            if (allAreaData[i].playerIx != playerIx) {
                continue;
            }
            areaTotalCount[chk[i]]++;
        }
        int max = 0;
        for (int i = 0; i < AREA_MAX; i++) {
            if (areaTotalCount[i] > max) {
                max = areaTotalCount[i];
            }
        }
        playersData[playerIx].areaTotalCount = max;
    }

    // map creation
    public void makeMap() {
        // serial number shuffle
        for (int i = 0; i < CELL_MAX; i++) {
            int r = (int) (Math.random() * CELL_MAX);
            int tmp = cellIndex[i];
            cellIndex[i] = cellIndex[r];
            cellIndex[r] = tmp;
        }
        // Cell initialization
        for (int i = 0; i < CELL_MAX; i++) {
            allBoardCells[i] = 0;
            neighborCellFlag[i] = false; // adjacent cell
        }
        int areaIx = 1;
        neighborCellFlag[(int) (Math.random() * CELL_MAX)] = true; // first cell

        while (true) {
            // Determine the penetration starting cell
            int cellIx = 0;
            int min = 9999;
            for (int i = 0; i < CELL_MAX; i++) {
                if (allBoardCells[i] > 0) {
                    continue;
                }
                if (cellIndex[i] > min) {
                    continue;
                }
                if (!neighborCellFlag[i]) {
                    continue;
                }
                min = cellIndex[i];
                cellIx = i;
            }
            if (min == 9999) {
                break;
            }

            // Soaking begins
            int ret = percolate(cellIx, 8, areaIx);
            if (ret == 0) {
                break;
            }
            areaIx++;
            if (areaIx >= AREA_MAX) {
                break;
            }
        }
        // Eliminate cells with area 1 in the ocean
        for (int i = 0; i < CELL_MAX; i++) {
            if (allBoardCells[i] > 0) {
                continue;
            }
            int f = 0;
            int a = 0;
            for (int k = 0; k < 6; k++) {
                int pos = neighborCell[i].direction[k];
                if (pos < 0) {
                    continue;
                }
                if (allBoardCells[pos] == 0) {
                    f = 1;
                } else {
                    a = allBoardCells[pos];
                }
            }
            if (f == 0) {
                allBoardCells[i] = a;
            }
        }
        // Area data initialization
        for (int i = 0; i < AREA_MAX; i++) {
            allAreaData[i] = new AreaData(AREA_MAX);
        }

        // area
        for (int i = 0; i < CELL_MAX; i++) {
            areaIx = allBoardCells[i];
            if (areaIx > 0)
                allAreaData[areaIx].size++;
        }
        // Erase areas with an area of 10 or less
        for (int i = 1; i < AREA_MAX; i++) {
            if (allAreaData[i].size <= 5)
                allAreaData[i].size = 0;
        }
        for (int i = 0; i < CELL_MAX; i++) {
            areaIx = allBoardCells[i];
            if (allAreaData[areaIx].size == 0) {
                allBoardCells[i] = 0;
            }
        }

        // Determine the center of the area
        for (int i = 1; i < AREA_MAX; i++) {
            allAreaData[i].left = XMAX;
            allAreaData[i].right = -1;
            allAreaData[i].top = YMAX;
            allAreaData[i].bottom = -1;
            allAreaData[i].lengthMin = 9999;
        }
        int cell = 0;
        for (int i = 0; i < YMAX; i++) {
            for (int j = 0; j < XMAX; j++) {
                areaIx = allBoardCells[cell];
                if (areaIx > 0) {
                    if (j < allAreaData[areaIx].left) {
                        allAreaData[areaIx].left = j;
                    }
                    if (j > allAreaData[areaIx].right) {
                        allAreaData[areaIx].right = j;
                    }
                    if (i < allAreaData[areaIx].top) {
                        allAreaData[areaIx].top = i;
                    }
                    if (i > allAreaData[areaIx].bottom) {
                        allAreaData[areaIx].bottom = i;
                    }
                }
                cell++;
            }
        }
        for (int i = 1; i < AREA_MAX; i++) {
            allAreaData[i].cx = ((allAreaData[i].left + allAreaData[i].right) / 2);
            allAreaData[i].cy = ((allAreaData[i].top + allAreaData[i].bottom) / 2);
        }
        cell = 0;
        int x, y;
        for (int i = 0; i < YMAX; i++) {
            for (int j = 0; j < XMAX; j++) {
                areaIx = allBoardCells[cell];
                if (areaIx > 0) {
                    // Distance from the center (avoid areas near the border as much as possible)
                    x = allAreaData[areaIx].cx - j;
                    if (x < 0) {
                        x = -x;
                    }
                    y = allAreaData[areaIx].cy - i;
                    if (y < 0) {
                        y = -y;
                    }
                    int len = x + y;
                    boolean neighborFlag = false;
                    for (int k = 0; k < 6; k++) {
                        int pos = neighborCell[cell].direction[k];
                        if (pos > 0) {
                            int areaNumber2 = allBoardCells[pos];
                            if (areaNumber2 != areaIx) {
                                neighborFlag = true;
                                // At the same time, adjacent data is also created.
                                allAreaData[areaIx].neighbors[areaNumber2] = true;
                            }
                        }
                    }
                    if (neighborFlag) {
                        len += 4;
                    }
                    // Center on something that is close to you
                    if (len < allAreaData[areaIx].lengthMin) {
                        allAreaData[areaIx].lengthMin = len;
                        allAreaData[areaIx].centerCellIx = i * XMAX + j;
                    }
                }
                cell++;
            }
        }

        // Decide on area troops
        for (int i = 0; i < AREA_MAX; i++) {
            allAreaData[i].playerIx = -1;
        }
        int playerIx = 0; // Belongs to the player
        int[] areaList = new int[AREA_MAX]; // area list
        while (true) {
            cell = 0;
            for (int i = 1; i < AREA_MAX; i++) {
                if (allAreaData[i].size == 0) {
                    continue;
                }
                if (allAreaData[i].playerIx >= 0) {
                    continue;
                }
                areaList[cell] = i;
                cell++;
            }
            if (cell == 0) {
                break;
            }
            areaIx = areaList[(int) (Math.random() % cell)];
            allAreaData[areaIx].playerIx = playerIx;
            playerIx++;
            if (playerIx >= maxPlayerCount) {
                playerIx = 0;
            }
        }
        // Creating area drawing line data
        for (int i = 0; i < AREA_MAX; i++) {
            chk[i] = 0;
        }
        for (int i = 0; i < CELL_MAX; i++) {
            int area = allBoardCells[i];
            if (area == 0) {
                continue;
            }
            if (chk[area] > 0) {
                continue;
            }
            for (int k = 0; k < 6; k++) {
                if (chk[area] > 0) {
                    break;
                }
                int n = neighborCell[i].direction[k];
                if (n >= 0) {
                    if (allBoardCells[n] != area) {
                        setAreaLine(i, k);
                        chk[area] = 1;
                    }
                }
            }
        }
        // Dice placement
        int numberOfAreas = 0;
        for (int i = 1; i < AREA_MAX; i++) {
            if (allAreaData[i].size > 0) {
                numberOfAreas++;
                allAreaData[i].dices = 1;
            }
        }
        numberOfAreas *= (placeDicesAverage - 1);
        int playerId = 0; // player
        for (int i = 0; i < numberOfAreas; i++) {
            int[] list = new int[AREA_MAX];
            cell = 0;
            for (int j = 1; j < AREA_MAX; j++) {
                if (allAreaData[j].size == 0) {
                    continue;
                }
                if (allAreaData[j].playerIx != playerId) {
                    continue;
                }
                if (allAreaData[j].dices >= 8) {
                    continue;
                }
                list[cell] = j;
                cell++;
            }
            if (cell == 0) {
                break;
            }
            areaIx = list[(int) (Math.random() * cell)];
            allAreaData[areaIx].dices++;
            playerId++;
            if (playerId >= maxPlayerCount) {
                playerId = 0;
            }
        }
    }

    // penetrate and create an area
    int percolate(int startCellIx, int maxCellCount, int areaIx) {
        if (maxCellCount < 3) {
            maxCellCount = 3;
        }

        int opos = startCellIx; // start cell

        // Adjacent flag
        for (int i = 0; i < CELL_MAX; i++) {
            neighborFlag[i] = 0;
        }

        int cellCount = 0; // Number of cells
        while (true) {
            allBoardCells[opos] = areaIx;
            cellCount++;
            // surrounding cells
            for (int i = 0; i < 6; i++) {
                int pos = neighborCell[opos].direction[i];
                if (pos < 0)
                    continue;
                neighborFlag[pos] = 1;
            }
            // Set the minimum serial number to the next cell in surrounding cells
            int min = 9999;
            for (int i = 0; i < CELL_MAX; i++) {
                if (neighborFlag[i] == 0) {
                    continue; // not adjacent
                }
                if (allBoardCells[i] > 0) {
                    continue; // Already an area
                }
                if (cellIndex[i] > min) {
                    continue; // Not the minimum serial number
                }
                min = cellIndex[i];
                opos = i;
            }
            if (min == 9999) {
                break;
            }
            if (cellCount >= maxCellCount) {
                break; // exceeded the given area
            }
        }
        // add adjacent cells
        for (int i = 0; i < CELL_MAX; i++) {
            if (neighborFlag[i] == 0)
                continue;
            if (allBoardCells[i] > 0) {
                continue; // Already an area
            }
            allBoardCells[i] = areaIx;
            cellCount++;
            // Furthermore, consider neighboring cells as candidates for the next area.
            for (int k = 0; k < 6; k++) {
                int pos = neighborCell[i].direction[k];
                if (pos < 0) {
                    continue;
                }
                neighborCellFlag[pos] = true;
            }
        }
        return cellCount;
    }

    // Area drawing line data creation
    void setAreaLine(int oldCell, int oldDirection) {
        int cell = oldCell;
        int direction = oldDirection;
        int areaIx = allBoardCells[cell]; // area number
        int count = 0;
        allAreaData[areaIx].lineDrawingCellIx[count] = cell;
        allAreaData[areaIx].lineDrawingDirection[count] = direction;
        count++;
        for (int i = 0; i < 100; i++) {
            direction++;
            if (direction >= 6) {
                direction = 0; // direction addition
            }
            int n = neighborCell[cell].direction[direction];
            if (n >= 0) {
                if (allBoardCells[n] == areaIx) {
                    // If the neighbor is in the same area, move the cell, direction minus 2
                    cell = n;
                    direction -= 2;
                    if (direction < 0) {
                        direction += 6;
                    }
                }
            }
            allAreaData[areaIx].lineDrawingCellIx[count] = cell;
            allAreaData[areaIx].lineDrawingDirection[count] = direction;
            count++;
            if (cell == oldCell && direction == oldDirection) {
                break;
            }
        }
        allAreaData[areaIx].linesCount = count;
    }

    int getNeighborCell(int cellIx, int direction) {
        int cellX = cellIx % XMAX;
        int cellY = cellIx / XMAX;
        int offset = cellY % 2;
        int ax = 0;
        int ay = 0;
        switch (direction) {
            case 0: // upper right
                ax = offset;
                ay = -1;
                break;
            case 1: // right
                ax = 1;
                break;
            case 2: // bottom right
                ax = offset;
                ay = 1;
                break;
            case 3: // bottom left
                ax = offset - 1;
                ay = 1;
                break;
            case 4: // left
                ax = -1;
                break;
            case 5: // upper left
                ax = offset - 1;
                ay = -1;
                break;
        }
        int x = cellX + ax;
        int y = cellY + ay;
        if (x < 0 || y < 0 || x >= XMAX || y >= YMAX)
            return -1;
        return y * XMAX + x;
    }

    // return current player id
    public int getCurrentPlayer() {
        return playersOrder[currentPlayer];
    }

    public boolean isCurrentPlayerComputer() {
        AI ai = this.ai[playersOrder[currentPlayer]];
        return ai != null;
    }

    // COM thinking - AI's move
    public int computerMakeMove() {
        AI ai = this.ai[playersOrder[currentPlayer]];
        return ai.ai_default(this);
    }

    // add to history
    // public void set_his(int from, int to, boolean res) {
    // int h = new HistoryData();
    // h.from = from;
    // h.to = to;
    // h.res = res;
    // history.add(h);
    // }
}
