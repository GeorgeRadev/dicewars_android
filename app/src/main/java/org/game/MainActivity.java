package org.game;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import org.game.dicewars.R;

public class MainActivity extends AppCompatActivity {
    View currentView;
    OpponentsView opponentsView;
    DiceWarView diceWarView;

    MainActivity context;

    int opponentsCount = 0;
    boolean init;

    public MainActivity() {
        context = this;
        init = false;
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
            CreateResourcesTask createResourcesTask = new CreateResourcesTask();
            createResourcesTask.start();
        }
    }

    class CreateResourcesTask extends Thread {
        @Override
        public void run() {
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
            runOnUiThread(() -> setOpponentsViewView());
        }
    }

    void setDiceView() {
        diceWarView.reset();
        setContentView(currentView = diceWarView);
    }

    void setOpponentsViewView() {
        setContentView(currentView = opponentsView);
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
}