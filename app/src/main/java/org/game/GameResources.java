package org.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;

import org.game.dicewars.AreaData;
import org.game.dicewars.Battle;
import org.game.dicewars.Game;
import org.game.dicewars.GameState;

public class GameResources {

    public static Game game;
    public static GameState gameState;
    public static int opponentsCount;
    public static Battle[] battle = new Battle[2];
    public static int lastClickedArea;
    static int[] cellX;
    static int[] cellY;

    static int[] playerColor;

    public static final int originalCellW = 27;
    public static final int originalCellH = 18;
    public static int viewWidth;
    public static int viewHeight;
    public static float density;
    public static int cellWidth;
    public static int cellHeight;
    public static int[] hexagonX;
    public static int[] hexagonY;

    // top left offset into the board painting
    static int offsetX;
    static int offsetY;

    public static int white;
    public static int black;
    public static int borderSelectedColor;
    public static int borderColor;
    public static int lightGray;
    public static int textBlack;
    public static Paint backgroundPaint;
    public static Bitmap backgroundImage;
    public static Paint textBigCenteredPaint;
    public static Paint textPaint;
    public static Paint whiteTextPaint;
    public static Paint blackTextPaint;
    public static Paint boardPaint;

    public static Bitmap boardImage;
    static int boardWidth;
    static int boardHeight;

    //sounds
    static MediaPlayer soundButton;
    static MediaPlayer soundFail;
    static MediaPlayer soundMyTurn;
    static MediaPlayer soundOver;
    static MediaPlayer soundSuccess;

    public static void init(DisplayMetrics displayMetrics) {
        game = new Game();

        lastClickedArea = 0;
        opponentsCount = -2;

        // landscape
        viewWidth = displayMetrics.widthPixels;
        viewHeight = displayMetrics.heightPixels;
        if (viewWidth < viewHeight) {
            viewWidth = displayMetrics.heightPixels;
            viewHeight = displayMetrics.widthPixels;
        }
        density = displayMetrics.density;

        // decide on the screen ratio
        if (viewWidth > 2 * viewHeight) {
            cellHeight = viewHeight / (game.YMAX + 2);
        } else {
            cellHeight = viewHeight / (game.YMAX + 9);
        }
        cellWidth = cellHeight * originalCellW / originalCellH;

        // draw honeycomb
        int s = 3 * viewWidth / viewHeight;
        hexagonX = new int[]{cellWidth / 2, cellWidth, cellWidth, cellWidth / 2, 0, 0, cellWidth / 2};
        hexagonY = new int[]{-s, s, cellHeight - s, cellHeight + s, cellHeight - s, s, -s};

        //board offset for painting
        offsetX = cellWidth;
        offsetY = cellHeight * 2;

        // create battle objects for attacking and defending player
        for (int i = 0; i < 2; i++) {
            battle[i] = new Battle();
        }

        playerColor = new int[8];
        playerColor[0] = Color.rgb(0xb3, 0x7f, 0xfe);
        playerColor[1] = Color.rgb(0xb3, 0xff, 0x01);
        playerColor[2] = Color.rgb(0x00, 0x93, 0x02);
        playerColor[3] = Color.rgb(0xff, 0x7f, 0xfe);
        playerColor[4] = Color.rgb(0xff, 0x7f, 0x01);
        playerColor[5] = Color.rgb(0xb3, 0xff, 0xfe);
        playerColor[6] = Color.rgb(0xff, 0xff, 0x01);
        playerColor[7] = Color.rgb(0xff, 0x58, 0x58);

        {
            // cell drawing position
            cellX = new int[game.XMAX * game.YMAX];
            cellY = new int[game.XMAX * game.YMAX];

            // cell positions
            int cellIx = 0;
            for (int i = 0; i < game.YMAX; i++) {
                for (int j = 0; j < game.XMAX; j++) {
                    cellX[cellIx] = j * cellWidth;
                    if (i % 2 != 0) {
                        cellX[cellIx] += cellWidth >> 1;
                    }
                    cellY[cellIx] = i * cellHeight;
                    cellIx++;
                }
            }
        }

        black = Color.rgb(0x00, 0x00, 0x00);
        white = Color.rgb(0xff, 0xff, 0xff);
        lightGray = Color.rgb(0xe3, 0xea, 0xf0);
        textBlack = Color.rgb(0x30, 0x30, 0x30);

        borderSelectedColor = Color.rgb(0xff, 0x00, 0x00);
        borderColor = Color.rgb(0x22, 0x22, 0x44);

        {
            Paint paint = new Paint();
            paint.setColor(white);
            paint.setTextSize(32 * displayMetrics.density);
            paint.setTextAlign(Paint.Align.CENTER);
            whiteTextPaint = paint;
        }
        {
            Paint paint = new Paint();
            paint.setColor(black);
            paint.setTextSize(32 * displayMetrics.density);
            paint.setTextAlign(Paint.Align.RIGHT);
            blackTextPaint = paint;
        }


        {// background
            backgroundPaint = new Paint();
            backgroundPaint.setAntiAlias(false);
            backgroundPaint.setFilterBitmap(false);
            backgroundPaint.setDither(false);


            Bitmap.Config config = Bitmap.Config.ARGB_8888;
            backgroundImage = Bitmap.createBitmap(viewWidth, viewHeight, config);
            Canvas canvas = new Canvas();
            canvas.setBitmap(backgroundImage);

            Paint paint = new Paint();
            paint.setColor(white);
            canvas.drawRoundRect(0, 0, viewWidth, viewHeight, 0, 0, paint);


            paint.setColor(lightGray);
            paint.setStrokeWidth(displayMetrics.density);
            for (int i = viewWidth / cellWidth; i >= -1; i--) {
                for (int j = viewHeight / cellHeight; j >= -1; j--) {
                    int x = i * cellWidth;
                    if (j % 2 != 0) {
                        x += cellWidth >> 1;
                    }
                    int y = j * cellHeight;
                    for (int k = 0; k < 3; k++) {
                        canvas.drawLine(x + hexagonX[k], y + hexagonY[k], x + hexagonX[k + 1], y + hexagonY[k + 1], paint);
                    }
                }
            }
        }
        {
            Paint paint = new Paint();
            paint.setColor(textBlack);
            paint.setTextSize(50 * displayMetrics.density);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setShadowLayer(4f, 4f, 4f, Color.LTGRAY);
            textBigCenteredPaint = paint;
        }
        {
            Paint paint = new Paint();
            paint.setColor(textBlack);
            paint.setTextSize(24 * displayMetrics.density);
            paint.setShadowLayer(1f, 2f, 2f, Color.LTGRAY);
            textPaint = paint;
        }
        {// board
            boardPaint = new Paint();
            boardPaint.setAntiAlias(false);
            boardPaint.setFilterBitmap(false);
            boardPaint.setDither(false);


            Bitmap.Config config = Bitmap.Config.ARGB_8888;
            boardWidth = cellWidth * (game.XMAX + 6);
            boardHeight = cellHeight * (game.YMAX + 4);
            boardImage = Bitmap.createBitmap(boardWidth, boardHeight, config);
        }
    }


    static void redrawFullMap() {
        Canvas canvas = new Canvas();
        canvas.setBitmap(boardImage);
        canvas.drawBitmap(backgroundImage, 0, 0, backgroundPaint);

        for (int i = 0; i < game.AREA_MAX; i++) {
            drawAreaShape(canvas, i, false);
        }
        for (int i = 0; i < game.AREA_MAX; i++) {
            drawAreaDice(canvas, i);
        }
    }

    static void newGame() {
        int opponents = GameResources.opponentsCount;
        if (opponents < 2) {
            game.maxPlayerCount = 2 + (int) (Math.random() * 6);
        } else {
            game.maxPlayerCount = opponents;
        }
        game.makeMap();
        GameResources.gameState = GameState.NOP;
        game.start();
    }

    public static void drawAreaShape(Canvas canvas, int area, boolean selected) {
        if (game.allAreaData[area].size == 0) {
            return;
        }
        int color;
        if (selected) {
            color = black;
        } else {
            color = playerColor[game.allAreaData[area].playerIx];
        }

        int lineColor;
        if (selected) {
            lineColor = borderSelectedColor;
        } else {
            lineColor = borderColor;
        }

        int p_ix = 0;
        int[] xPoints = new int[100];
        int[] yPoints = new int[100];


        int cnt = 0;
        int c = game.allAreaData[area].lineDrawingCellIx[cnt];
        int d = game.allAreaData[area].lineDrawingDirection[cnt];

        int px = hexagonX[d];
        int py = hexagonY[d];

        xPoints[p_ix] = offsetX + cellX[c] + px;
        yPoints[p_ix] = offsetY + cellY[c] + py;

        p_ix++;
        for (int i = 0; i < 100; i++) {
            // draw a line first
            px = hexagonX[d + 1];
            py = hexagonY[d + 1];
            xPoints[p_ix] = offsetX + cellX[c] + px;
            yPoints[p_ix] = offsetY + cellY[c] + py;
            p_ix++;
            cnt++;
            c = game.allAreaData[area].lineDrawingCellIx[cnt];
            d = game.allAreaData[area].lineDrawingDirection[cnt];
            if (c == game.allAreaData[area].lineDrawingCellIx[0]
                    && d == game.allAreaData[area].lineDrawingDirection[0]) {
                break;
            }
        }

        {// paint area
            Paint areaPaint = new Paint();
            areaPaint.setColor(color);
            areaPaint.setStyle(Paint.Style.FILL);
            fillHexagons(canvas, areaPaint, area);
        }
        {//draw border
            Paint linePaint = new Paint();
            linePaint.setColor(lineColor);
            linePaint.setStrokeWidth(density * 2);
            for (int i = 0, l = p_ix - 1; i < l; i++) {
                canvas.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1], linePaint);
            }
        }
    }

    static void fillHexagons(Canvas canvas, Paint areaPaint, int area) {
        AreaData areaData = game.allAreaData[area];

        Path areaPath = new Path();
        areaPath.setFillType(Path.FillType.EVEN_ODD);

        for (int j = areaData.top; j <= areaData.bottom; j++) {
            for (int i = areaData.left; i <= areaData.right; i++) {
                int c = j * game.XMAX + i;
                if (area == game.allBoardCells[c]) {
                    int px = hexagonX[0];
                    int py = hexagonY[0];
                    int x = cellX[c] + px + offsetX;
                    int y = cellY[c] + py + offsetY;
                    areaPath.moveTo(x, y);
                    for (int k = 1, l = hexagonX.length; k < l; k++) {
                        px = hexagonX[k];
                        py = hexagonY[k];
                        x = cellX[c] + px + offsetX;
                        y = cellY[c] + py + offsetY;
                        areaPath.lineTo(x, y);
                    }
                    canvas.drawPath(areaPath, areaPaint);
                }
            }
        }
    }

    public static void drawAreaDice(Canvas canvas, int area) {
        if (game.allAreaData[area].size == 0) {
            return;
        }

        int n = game.allAreaData[area].centerCellIx;
        int x = cellX[n];
        int y = cellY[n] + cellHeight;
        canvas.drawText(game.allAreaData[area].dices + "", x + offsetX, y + offsetY, textPaint);
    }

    static void start_battle() {
        int[] an = new int[]{game.attackAreaFrom, game.attackAreaTo};
        for (int i = 0; i < 2; i++) {
            battle[i].playerIx = game.allAreaData[an[i]].playerIx;
            battle[i].diceCount = game.allAreaData[an[i]].dices;

            battle[i].sum = 0;
            for (int j = 0; j < battle[i].diceCount; j++) {
                battle[i].riceRoll[j] = 1 + (int) (Math.random() * 5);
                battle[i].sum += battle[i].riceRoll[j];
            }
        }
    }

    // true - win, false - gameover, null - game goes on
    static Boolean after_battle() {
        int playerIx1 = game.allAreaData[game.attackAreaFrom].playerIx;
        int playerIx2 = game.allAreaData[game.attackAreaTo].playerIx;
        boolean defeat = battle[0].sum > battle[1].sum;
        if (defeat) {
            game.allAreaData[game.attackAreaTo].dices = game.allAreaData[game.attackAreaFrom].dices - 1;
            game.allAreaData[game.attackAreaFrom].dices = 1;
            game.allAreaData[game.attackAreaTo].playerIx = playerIx1;
            game.calculateTotalAreaCount(playerIx1);
            game.calculateTotalAreaCount(playerIx2);
            game.calculatePlayersAreas();
            GameResources.soundSuccess.start();
        } else {
            game.allAreaData[game.attackAreaFrom].dices = 1;
            GameResources.soundFail.start();
        }

        // game.set_his(game.attackAreaFrom, game.attackAreaTo, defeat);

        if (game.playersData[game.userPlayerIx].areaTotalCount == 0) {
            return Boolean.FALSE;
        } else {
            int c = 0;
            for (int i = 0; i < game.maxPlayerCount; i++) {
                if (game.playersData[i].areaTotalCount > 0) {
                    c++;
                }
            }
            if (c == 1) {
                return Boolean.FALSE;
            } else {
                return null;
            }
        }
    }

    static void start_supply() {
        int pn = game.getCurrentPlayer();
//		game.player[pn].stock = 64;
        game.calculateTotalAreaCount(pn);
        game.playersData[pn].stock += game.playersData[pn].areaTotalCount;
        if (game.playersData[pn].stock > game.STOCK_MAX) {
            game.playersData[pn].stock = game.STOCK_MAX;
        }
    }

    // return true if need to supply more dices
    static boolean supply_dice() {
        int playerIx = game.getCurrentPlayer();
        int[] list = new int[game.AREA_MAX];
        int c = 0;
        for (int i = 0; i < game.AREA_MAX; i++) {
            if (game.allAreaData[i].size == 0) {
                continue;
            }
            if (game.allAreaData[i].playerIx != playerIx) {
                continue;
            }
            if (game.allAreaData[i].dices >= 8) {
                continue;
            }
            list[c] = i;
            c++;
        }
        if (c == 0 || game.playersData[playerIx].stock <= 0) {
            // next_player();
            return false;
        }

        game.playersData[playerIx].stock--;
        int areaNumber = list[(int) (Math.random() * c)];
        game.allAreaData[areaNumber].dices++;

        //drawAreaDice(areaNumber);
        //repaint();

        // game.set_his(areaNumber, 0, false);
        return true;
    }

    static void next_player() {
        for (int i = 0; i < game.maxPlayerCount; i++) {
            game.currentPlayer++;
            if (game.currentPlayer >= game.maxPlayerCount) {
                game.currentPlayer = 0;
            }
            int pn = game.playersOrder[game.currentPlayer];
            if (game.playersData[pn].areaTotalCount > 0) {
                break;
            }
        }
        if (game.playersOrder[game.currentPlayer] == game.userPlayerIx) {
            GameResources.soundMyTurn.start();
        }
        start_player();
    }

    static void start_player() {
        if (game.playersOrder[game.currentPlayer] == game.userPlayerIx) {
            gameState = GameState.HUMAN_TURN;
        } else {
            gameState = GameState.AI_TURN;
        }
    }


    static boolean firstClick() {
        int playerIx = game.getCurrentPlayer();
        int areaIx = lastClickedArea;
        if (areaIx < 0) {
            return false;
        }
        lastClickedArea = 0;

        if (game.allAreaData[areaIx].playerIx != playerIx) {
            return false;
        }
        if (game.allAreaData[areaIx].dices <= 1) {
            return false;
        }

        game.attackAreaFrom = areaIx;

        Canvas canvas = new Canvas();
        canvas.setBitmap(boardImage);
        drawAreaShape(canvas, areaIx, true);

        return true;
    }

    static boolean second_click() {
        int p = game.playersOrder[game.currentPlayer];
        int an = lastClickedArea;
        if (an < 0) {
            return false;
        }
        lastClickedArea = 0;

        if (an == game.attackAreaFrom) {
            return false;
        }
        if (game.allAreaData[an].playerIx == p) {
            return false;
        }
        if (!game.allAreaData[an].neighbors[game.attackAreaFrom]) {
            return false;
        }

        game.attackAreaTo = an;

        Canvas canvas = new Canvas();
        canvas.setBitmap(boardImage);
        drawAreaShape(canvas, an, true);
        return true;
    }


    public static void selectArea(int an) {
        Canvas canvas = new Canvas();
        canvas.setBitmap(boardImage);
        drawAreaShape(canvas, an, true);
        drawAreaDice(canvas, an);
    }

    public static int clickToArea(int x, int y) {
        y -= offsetY;
        if (y < 0) {
            return 0;
        }
        y = y / cellHeight;
        x -= offsetX;
        if (y % 2 != 0) {
            x -= cellWidth / 2;
        }
        if (x < 0) {
            return 0;
        }
        x = x / cellWidth;
        if (x >= 0 && x < game.XMAX && y >= 0 && y < game.YMAX) {
            int an = game.allBoardCells[y * game.XMAX + x];
            //System.out.println("x,y = " + x + "," + y + " an=" + an);
            if (an > 0) {
                return an;
            }
        }
        return 0;
    }
}
