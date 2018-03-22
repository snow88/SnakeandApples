package com.example.dell.game;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;

public class MainActivity extends AppCompatActivity {
    GameEngine gme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display d = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        d.getSize(size);

        gme = new GameEngine(this, size);
        setContentView(gme);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gme.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gme.resume();
    }
}
