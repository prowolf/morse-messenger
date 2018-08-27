package com.prowolf.morsemessenger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.prowolf.shared.Client;
import com.prowolf.shared.MorseGestureDetector;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends Activity {

    private static final long[] pattern = {0, 1000};
    private View panel;
    private Vibrator vibrator;
    ToneGenerator toneGenerator;
    private GestureDetector gestureDetector;
    private MorseGestureDetector morseGestureDetector;
    private SharedPreferences settings;
    private Thread networking;
    private Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this.getApplicationContext());
        client = new Client(settings.getString("host", "tcp://127.0.0.1:8484"));
        networking = new Thread(client);
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
                    activate(true);
                    client.sendMessage("t");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    deactivate(true);
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
                        activate(false);
                    } else if (line.equals("f")) {
                        deactivate(false);
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
    }

    protected void onResume(){
        super.onResume();
        if (client.getConnection().toString() != settings.getString("host", "tcp://127.0.0.1:8484")) {
            networking.interrupt();
            client = new Client(settings.getString("host", "tcp://127.0.0.1:8484"));
            networking = new Thread(client);
            networking.start();
        }
    }

    private void activate(boolean me) {
        String setting = settings.getString("flash", "Me");
        if ((setting.equals("Me") == me || setting.equals("All"))&& !setting.equals("Off"))
            panel.setBackgroundColor(getResources().getColor(android.R.color.white));
        setting = settings.getString("vibrate", "Others");
        if ((setting.equals("Me") == me || setting.equals("All"))&& !setting.equals("Off"))
            vibrator.vibrate(pattern, 0);
        setting = settings.getString("sound", "Others");
        if ((setting.equals("Me") == me || setting.equals("All"))&& !setting.equals("Off")) {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_DIAL_TONE_LITE);
        }
    }

    private void deactivate(boolean me) {
        String setting = settings.getString("flash", "Me");
        if ((setting.equals("Me") == me || setting.equals("All")) && !setting.equals("Off"))
            panel.setBackgroundColor(getResources().getColor(android.R.color.black));
        setting = settings.getString("vibrate", "Others");
        if ((setting.equals("Me") == me || setting.equals("All")) && !setting.equals("Off"))
            vibrator.cancel();
        setting = settings.getString("sound", "Others");
        if ((setting.equals("Me") == me || setting.equals("All")) && !setting.equals("Off")) {
            toneGenerator.stopTone();
            toneGenerator.release();
        }
    }
}
