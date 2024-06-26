package org.game;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import org.game.dicewars.R;

public class MainActivity extends AppCompatActivity implements Runnable {
    View currentView;
    OpponentsView opponentsView;
    DiceWarView diceWarView;

    Thread createResources;

    MainActivity context;

    int opponentsCount = 0;
    boolean init;

    public MainActivity() {
        context = this;
        init = false;
        createResources = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        if (!init && savedInstanceState != null) {
            init = true;
            createResources = new Thread(this);
            createResources.start();
        }
    }


    void setDiceView() {
        diceWarView.reset();
        setContentView(currentView = diceWarView);
    }

    void setOpponentsView() {
        setContentView(currentView = opponentsView);
        opponentsView.invalidate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentView != null && currentView == diceWarView) {
            ((DiceWarView) currentView).pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentView != null && currentView == diceWarView) {
            ((DiceWarView) currentView).resume();
        }
    }

    @Override
    public void run() {
        try {
            // give a little time to render
            Thread.sleep(600);
        } catch (InterruptedException e) {
            // ok
        }
        // initialize game resources
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        GameResources.init(displayMetrics);

        GameResources.soundButton = MediaPlayer.create(context, R.raw.button);
        GameResources.soundFail = MediaPlayer.create(context, R.raw.fail);
        GameResources.soundMyTurn = MediaPlayer.create(context, R.raw.myturn);
        GameResources.soundOver = MediaPlayer.create(context, R.raw.over);
        GameResources.soundSuccess = MediaPlayer.create(context, R.raw.success);

        opponentsView = new OpponentsView(context);
        diceWarView = new DiceWarView(context);

        init = true;
        createResources = null;
        runOnUiThread(() -> setOpponentsView());
    }
}