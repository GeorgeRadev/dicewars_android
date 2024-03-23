package org.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

public class OpponentsView extends View {

    final MainActivity main;
    final Rect[] opponentRects;
    final Paint selectedPaint;

    public OpponentsView(Context context) {
        super(context);
        main = (MainActivity) context;

        int halfScreenW = (GameResources.viewWidth >> 1);
        int halfScreenH = (GameResources.viewHeight >> 1);
        int spacing = halfScreenH / 7;
        int spacingHalf = spacing >> 1;
        int left = halfScreenW - 6 * spacing + spacingHalf;
        int top = halfScreenH;

        selectedPaint = new Paint();
        selectedPaint.setColor(GameResources.playerColor[0]);

        opponentRects = new Rect[8];
        for (int i = 0; i < 8; i++) {
            opponentRects[i] = new Rect();
        }
        for (int i = 0; i < 8; i++) {
            Rect r = opponentRects[i];
            r.left = left + ((i < 4) ? (3 * i * spacing) : (3 * (i - 4) * spacing));
            r.top = ((i < 4) ? (top) : (top + 3 * spacing));

            r.right = r.left + 2 * spacing;
            r.bottom = r.top + 2 * spacing;
        }
    }

    private static final float[][] dots = {
            //1
            {0, 0},
            //2
            {0.5f, 0.5f, -0.5f, -0.5f},
            //3
            {0, 0, 0.6f, 0.6f, -0.6f, -0.6f},
            //4
            {0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f},
            //5
            {0, 0, 0.6f, 0.6f, -0.6f, -0.6f, 0.6f, -0.6f, -0.6f, 0.6f},
            //6
            {0, 0.6f, 0, -0.6f, 0.6f, 0.6f, -0.6f, -0.6f, 0.6f, -0.6f, -0.6f, 0.6f},
            //7
            {0, 0, 0, 0.6f, 0, -0.6f, 0.6f, 0.6f, -0.6f, -0.6f, 0.6f, -0.6f, -0.6f, 0.6f},
    };

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(GameResources.backgroundImage, 0.0f, 0.0f, GameResources.backgroundPaint);
        canvas.drawText("DICE WARS", GameResources.viewWidth >> 1, (GameResources.viewHeight >> 1) - 70 * GameResources.density, GameResources.textBigCenteredPaint);
        canvas.drawText("Select opponents count:", (GameResources.viewWidth >> 1) - 130 * GameResources.density, (GameResources.viewHeight >> 1) - 30 * GameResources.density, GameResources.textPaint);

        float roundness = GameResources.density * 4;
        for (int i = 0; i < 8; i++) {
            Rect r = opponentRects[i];
            Paint paint = GameResources.textPaint;
            if (GameResources.opponentsCount == i + 1) {
                paint = selectedPaint;
            }
            canvas.drawRoundRect(r.left, r.top, r.right, r.bottom, roundness, roundness, paint);
            int xc = (r.left + r.right) >> 1;
            int w = (r.right - r.left) * 5 / 10;
            int rad = w / 6;
            int yc = (r.top + r.bottom) >> 1;
            if (i > 0) {
                float[] dot = dots[i - 1];
                for (int j = 0, l = i << 1; j < l; j += 2) {
                    canvas.drawCircle(xc + w * dot[j], yc + w * dot[j + 1], rad, GameResources.whiteTextPaint);
                }
            } else {
                canvas.drawText("?", xc, yc + (GameResources.whiteTextPaint.getTextSize() / 3), GameResources.whiteTextPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        int selection = -1;
        for (int i = 0; i < 8; i++) {
            Rect r = opponentRects[i];
            if (r.left < x && x < r.right && r.top < y && y < r.bottom) {
                selection = i;
                break;
            }
        }
        if (selection >= 0) {
            invalidate();
            GameResources.opponentsCount = 1 + selection;
            GameResources.soundMyTurn.start();
            main.setDiceView();
        }
        return false;
    }
}