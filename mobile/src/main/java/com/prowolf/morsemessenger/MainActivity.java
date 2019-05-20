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

public class MainActivity extends Activity {

    private static final long[] pattern = {0, 1000};
    private View panel;
    private View progressBar;
    private Vibrator vibrator;
    ToneGenerator toneGenerator;
    private GestureDetector gestureDetector;
    private MorseGestureDetector morseGestureDetector;
    private SharedPreferences settings;
    private Thread networking;
    private Client client;
    private boolean meflash;
    private boolean mevibrate;
    private boolean mesound;
    private boolean oflash;
    private boolean ovibrate;
    private boolean osound;
    private boolean canactive;
    final String ACTIVATE = "t";
    final String DEACTIVATE = "f";
    final String QUERRY = "q";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this.getApplicationContext());
        progressBar = findViewById(R.id.progressBar);
        client = new Client(settings.getString("host", "tcp://127.0.0.1:8484"), progressBar, findViewById(R.id.textView), getResources().getString(R.string.attempts_text_placeholder), getResources().getString(R.string.attempts_failed));
        networking = new Thread(client);
        networking.start();
        meflash = false;
        mevibrate = false;
        mesound = false;
        oflash = false;
        ovibrate = false;
        osound = false;
        canactive = true;
        morseGestureDetector = new MorseGestureDetector(getApplicationContext().getResources().getDisplayMetrics());
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
                    if (line.equals(ACTIVATE)) {
                        activate(false);
                    } else if (line.equals(DEACTIVATE)) {
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

    protected void onPause() {
        super.onPause();
        deactivate(true);
        deactivate(false);
        canactive = false;
    }

    protected void onResume(){
        super.onResume();
        if (client.getConnection().toString() != settings.getString("host", "tcp://127.0.0.1:8484")) {
            networking.interrupt();
            client = new Client(settings.getString("host", "tcp://127.0.0.1:8484"), progressBar, findViewById(R.id.textView), getResources().getString(R.string.attempts_text_placeholder), getResources().getString(R.string.attempts_failed));
            networking = new Thread(client);
            networking.start();
            progressBar.setVisibility(View.VISIBLE);
        }
        canactive = true;
        client.sendMessage(QUERRY);
    }

    private void activate(boolean me) {
        if (canactive) {
            String setting = settings.getString("flash", "Me");
            if ((setting.equals("Me") == me || setting.equals("All")) && !setting.equals("Off")) {
                if (me) {
                    meflash = true;
                } else {
                    oflash = true;
                }
                if (!(meflash && oflash))
                    panel.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
            setting = settings.getString("vibrate", "Others");
            if ((setting.equals("Me") == me || setting.equals("All")) && !setting.equals("Off")) {
                if (me) {
                    mevibrate = true;
                } else {
                    ovibrate = true;
                }
                if (!(mevibrate && ovibrate))
                    vibrator.vibrate(pattern, 0);
            }
            setting = settings.getString("sound", "Others");
            if ((setting.equals("Me") == me || setting.equals("All")) && !setting.equals("Off")) {
                if (me) {
                    mesound = true;
                } else {
                    osound = true;
                }
                if (!(mesound && osound)) {
                    toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_DIAL_TONE_LITE);
                }
            }
        }
    }

    private void deactivate(boolean me) {
        if (canactive) {
            String setting = settings.getString("flash", "Me");
            if ((setting.equals("Me") == me || setting.equals("All")) && !setting.equals("Off") && (meflash || oflash)) {
                if (me) {
                    meflash = false;
                } else {
                    oflash = false;
                }
                if (!meflash && !oflash)
                    panel.setBackgroundColor(getResources().getColor(android.R.color.black));
            }
            setting = settings.getString("vibrate", "Others");
            if ((setting.equals("Me") == me || setting.equals("All")) && !setting.equals("Off") && (mevibrate || ovibrate)) {
                if (me) {
                    mevibrate = false;
                } else {
                    ovibrate = false;
                }
                if (!mevibrate && !ovibrate)
                    vibrator.cancel();
            }
            setting = settings.getString("sound", "Others");
            if ((setting.equals("Me") == me || setting.equals("All")) && !setting.equals("Off") && (mesound || osound)) {
                if (me) {
                    mesound = false;
                } else {
                    osound = false;
                }
                if (!mesound && !osound) {
                    toneGenerator.stopTone();
                    toneGenerator.release();
                }
            }
        }
    }
}
