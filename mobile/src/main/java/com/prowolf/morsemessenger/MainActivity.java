package com.prowolf.morsemessenger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.prowolf.shared.Client;
import com.prowolf.shared.MorseGestureDetector;

public class MainActivity extends Activity {

    private static final long[] pattern = {0, 1000};
    private View panel;
    private Vibrator vibrator;
    ToneGenerator toneGenerator;
    private GestureDetector gestureDetector;
    private MorseGestureDetector morseGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Client client = new Client("tcp://192.168.1.2:8484");
        Thread networking = new Thread(client);
        networking.start();
        morseGestureDetector = new MorseGestureDetector();
        gestureDetector = new GestureDetector(morseGestureDetector);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (vibrator.hasVibrator()) {
                Log.v("Can Vibrate", "YES");
            } else {
                Log.v("Can Vibrate", "NO");
            }
        }
        panel = findViewById(R.id.panel);
        panel.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    activate();
                    client.sendMessage("t");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    deactivate();
                    client.sendMessage("f");
                    view.performClick();
                }
                return true;
            }
        });
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                String line = client.getNextMessage();
                if (line != null) {
                    if (line.equals("t")) {
                        activate();
                    } else if (line.equals("f")) {
                        deactivate();
                    }
                }
                if (morseGestureDetector.shouldSettings) {
                    morseGestureDetector.shouldSettings = false;
                    Intent intentMain = new Intent(MainActivity.this ,
                            SettingsActivity.class);
                    MainActivity.this.startActivity(intentMain);
                }
                handler.post(this);
            }
        });
        toneGenerator = new ToneGenerator(ToneGenerator.TONE_DTMF_A, 50);
    }

    private void activate() {
        panel.setBackgroundColor(getResources().getColor(android.R.color.white));
        vibrator.vibrate(pattern, 0);
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT);
    }

    private void deactivate() {
        panel.setBackgroundColor(getResources().getColor(android.R.color.black));
        vibrator.cancel();
        toneGenerator.stopTone();
    }
}
