package org.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.game.dicewars.GameState;

public class DiceWarView extends SurfaceView implements Runnable {

    boolean hasTopButton;

    boolean battleResult = false;

    final int boardLeft;
    final int boardTop;

    final Rect topButtonRect = new Rect();
    final Rect bottomButtonRect = new Rect();

    final Rect[] countRects = new Rect[8];
    final Rect[] battleRects = new Rect[2];
    final Paint countPaint = new Paint();
    final Paint resultPaint = new Paint();

    private Thread thread = null;

    public DiceWarView(Context context) {
        super(context);

        boardLeft = GameResources.cellWidth * 6;
        boardTop = 0;

        { // top right button
            Rect r = topButtonRect;
            r.left = GameResources.viewWidth - 8 * GameResources.cellWidth;
            r.top = 2 * GameResources.cellHeight;
            r.right = r.left + 6 * GameResources.cellWidth;
            r.bottom = r.top + 4 * GameResources.cellHeight;
        }
        {// bottom right button
            Rect r = bottomButtonRect;
            r.left = GameResources.viewWidth - 8 * GameResources.cellWidth;
            r.top = GameResources.viewHeight - 6 * GameResources.cellHeight;
            r.right = r.left + 6 * GameResources.cellWidth;
            r.bottom = r.top + 4 * GameResources.cellHeight;
        }
        {// player dices count buttons
            for (int i = 0; i < 8; i++) {
                Rect r = new Rect();
                r.left = GameResources.cellWidth + 3 / 2;
                r.top = (i * 4 + 2) * GameResources.cellHeight;
                r.right = r.left + 4 * GameResources.cellWidth;
                r.bottom = r.top + GameResources.cellHeight * 10 / 3;
                countRects[i] = r;
            }
        }
        {// battle rects
            for (int i = 0; i < 2; i++) {
                Rect r = new Rect();
                r.left = topButtonRect.left;
                r.top = (i * 4 + 2) * GameResources.cellHeight + topButtonRect.bottom;
                r.right = topButtonRect.right;
                r.bottom = r.top + GameResources.cellHeight * 10 / 3;
                battleRects[i] = r;
            }
        }

        resultPaint.set(GameResources.textPaint);

        setBackgroundColor(Color.TRANSPARENT);
        setZOrderOnTop(true);

        reset();
    }

    public void reset() {
        //select mode
        hasTopButton = true;

        //game mode
        battleResult = false;

        GameResources.gameState = GameState.NOP;
        GameResources.newGame();
        GameResources.redrawFullMap();

        pause();
        repaint();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawMethod(canvas);
    }

    public void drawMethod(Canvas canvas) {
        // draw background
        canvas.drawBitmap(GameResources.backgroundImage,
                0.0f, 0.0f, GameResources.backgroundPaint);
        // draw map
        canvas.drawBitmap(GameResources.boardImage,
                boardLeft,
                boardTop, GameResources.boardPaint);

        // draw buttons
        float roundness = GameResources.density * 4;

        if (hasTopButton) {
            Rect r = topButtonRect;
            int xc = (r.left + r.right) >> 1;
            String text;
            if (GameResources.gameState == GameState.WIN) {
                text = "Won";
                resultPaint.setColor(GameResources.wonColor);
            } else if (GameResources.gameState == GameState.GAME_OVER) {
                text = "Lost";
                resultPaint.setColor(GameResources.lostColor);
            } else {
                text = GameResources.gameState == GameState.NOP ? "Play" : "Ready";
                resultPaint.setColor(GameResources.textBlack);
            }
            canvas.drawRoundRect(r.left, r.top, r.right, r.bottom, roundness, roundness, resultPaint);
            canvas.drawText(text,
                    xc, r.top + (GameResources.whiteTextPaint.getTextSize()),
                    GameResources.whiteTextPaint);
        }
        {
            Rect r = bottomButtonRect;
            canvas.drawRoundRect(r.left, r.top, r.right, r.bottom, roundness, roundness, GameResources.textPaint);
            int xc = (r.left + r.right) >> 1;
            boolean isNext = GameResources.gameState == GameState.NOP;
            canvas.drawText(isNext ? "Next" : "Back",
                    xc, r.top + (GameResources.whiteTextPaint.getTextSize()), GameResources.whiteTextPaint);
        }

        // draw area counts per player
        for (int i = 0, l = GameResources.game.maxPlayerCount; i < l; i++) {
            Rect r = countRects[i];
            countPaint.setColor(GameResources.playerColor[i]);
            canvas.drawRoundRect(r.left, r.top, r.right, r.bottom, roundness, roundness, countPaint);
            int xc = (r.left + r.right) >> 1;
            String text = "" + GameResources.game.playersData[i].areaCount;
            canvas.drawText(text,
                    xc + GameResources.cellWidth,
                    r.top + (GameResources.whiteTextPaint.getTextSize()) - GameResources.cellHeight / 3,
                    GameResources.blackTextPaint);
        }

        // battle result
        if (battleResult) {
            for (int i = 0; i < 2; i++) {
                Rect r = battleRects[i];
                countPaint.setColor(GameResources.playerColor[GameResources.battle[i].playerIx]);
                canvas.drawRoundRect(r.left, r.top, r.right, r.bottom, roundness, roundness, countPaint);
                int xc = (r.left + r.right) >> 1;
                String text = "" + GameResources.battle[i].sum;
                canvas.drawText(text,
                        xc + GameResources.cellWidth,
                        r.top + (GameResources.whiteTextPaint.getTextSize()) - GameResources.cellHeight / 3,
                        GameResources.blackTextPaint);
            }
        }
    }

    public void repaint() {
        hasTopButton = !(GameResources.gameState == GameState.AI_TURN ||
                GameResources.gameState == GameState.AI_TURN_END ||
                GameResources.gameState == GameState.HUMAN_TURN_END);

        Canvas canvas = null;
        SurfaceHolder surfaceHolder = null;
        try {
            surfaceHolder = getHolder();
            canvas = surfaceHolder.lockCanvas();
            synchronized (this) {
                if (canvas != null) {
                    drawMethod(canvas);
                }
            }
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        boolean gameEnded = GameResources.gameState == GameState.WIN || GameResources.gameState == GameState.GAME_OVER;

        {// bottom button click
            Rect r = bottomButtonRect;
            if (r.left < x && x < r.right && r.top < y && y < r.bottom) {
                if (GameResources.gameState == GameState.NOP) {
                    // next map
                    GameResources.newGame();
                    GameResources.redrawFullMap();
                    hasTopButton = true;
                    battleResult = false;
                    pause();
                    repaint();
                    GameResources.soundButton.start();
                    return false;

                } else {
                    // back
                    pause();
                    GameResources.soundFail.start();
                    ((MainActivity) getContext()).setOpponentsView();
                    return false;
                }
            }
        }

        if (hasTopButton && !gameEnded) {
            Rect r = topButtonRect;
            if (r.left < x && x < r.right && r.top < y && y < r.bottom) {
                if (GameResources.gameState == GameState.NOP) {
                    // start the game
                    GameResources.soundButton.start();
                    GameResources.start_player();
                    resume();
                    return false;

                } else {
                    // next turn
                    GameResources.soundButton.start();
                    repaint();
                    GameResources.gameState = GameState.HUMAN_TURN_END;
                    resume();
                    return false;
                }
            }

            // select areas to battle
            if (GameResources.gameState == GameState.HUMAN_WAIT) {
                int areaIx = GameResources.clickToArea(x - boardLeft, y - boardTop);
                if (areaIx >= 0) {
                    GameResources.lastClickedArea = areaIx;
                    if (GameResources.firstClick()) {
                        GameResources.soundButton.start();
                        GameResources.gameState = GameState.HUMAN_ATACK;
                    } else {
                        GameResources.gameState = GameState.HUMAN_WAIT;
                        GameResources.redrawFullMap();
                    }
                    repaint();
                    return false;
                }
            } else if (GameResources.gameState == GameState.HUMAN_ATACK) {
                int areaIx = GameResources.clickToArea(x - boardLeft, y - boardTop);
                if (areaIx >= 0) {
                    GameResources.lastClickedArea = areaIx;
                    if (GameResources.second_click()) {
                        GameResources.soundButton.start();
                        GameResources.gameState = GameState.HUMAN_BATTLE;
                    } else {
                        GameResources.gameState = GameState.HUMAN_WAIT;
                        GameResources.redrawFullMap();
                    }
                    repaint();
                    if (GameResources.gameState == GameState.HUMAN_BATTLE) {
                        resume();
                    }
                    return false;
                }
            }
        }
        return false;
    }

    public void resume() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void pause() {
        if (thread != null) {
            thread.interrupt();
            while (true) {
                try {
                    thread.join();
                    break;
                } catch (InterruptedException e) {
                    //ok
                }
            }
            thread = null;
        }
    }

    @Override
    public void run() {
        // game loop
        try {
            while (true) {
                switch (GameResources.gameState) {
                    case NOP:
                        break;

                    case HUMAN_TURN:
                        GameResources.lastClickedArea = 0;
                        repaint();
                        GameResources.gameState = GameState.HUMAN_WAIT;
                        continue;

                    case HUMAN_WAIT:
                        break;

                    case HUMAN_ATACK:
                        break;

                    case HUMAN_BATTLE: {
                        GameResources.start_battle();
                        battleResult = true;
                        repaint();
                        Thread.sleep(100);
                        Boolean outcome = GameResources.after_battle();
                        GameResources.redrawFullMap();
                        if (outcome == null) {
                            GameResources.gameState = GameState.HUMAN_WAIT;
                        } else {
                            GameResources.gameState = outcome ? GameState.WIN : GameState.GAME_OVER;
                        }
                        repaint();
                    }
                    break;

                    case AI_TURN:
                        int nextMove = GameResources.game.computerMakeMove();
                        if (nextMove > 0) {
                            GameResources.selectArea(GameResources.game.attackAreaFrom);
                            repaint();
                            Thread.sleep(50);
                            GameResources.selectArea(GameResources.game.attackAreaTo);
                            repaint();
                            GameResources.start_battle();

                            Thread.sleep(100);
                            battleResult = true;
                            repaint();
                            Boolean outcome = GameResources.after_battle();
                            GameResources.redrawFullMap();
                            if (outcome != null) {
                                GameResources.gameState = outcome ? GameState.WIN : GameState.GAME_OVER;
                            }
                            repaint();
                        } else {
                            GameResources.gameState = GameState.AI_TURN_END;
                        }
                        continue;

                    case AI_TURN_END:
                        GameResources.start_supply();
                        while (GameResources.supply_dice()) {
                            //GameResources.redrawFullMap();
                            //repaint();
                            //Thread.sleep(20);
                        }
                        GameResources.redrawFullMap();
                        repaint();
                        GameResources.next_player();
                        continue;

                    case HUMAN_TURN_END:
                        GameResources.start_supply();
                        while (GameResources.supply_dice()) {
                            // GameResources.redrawFullMap();
                            // repaint();
                            // Thread.sleep(20);
                        }
                        GameResources.redrawFullMap();
                        repaint();
                        GameResources.next_player();
                        continue;

                    case WIN:
                        repaint();
                        GameResources.soundSuccess.start();
                        break;

                    case GAME_OVER:
                        repaint();
                        GameResources.soundFail.start();
                        break;
                }
                break;
            }
        } catch (InterruptedException e) {
            // ok
        }
        thread = null;
    }
}
