package com.prowolf.morsemessenger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import com.prowolf.shared.BTTelegraph;
import com.prowolf.shared.GestureListener;
import com.prowolf.shared.IPTelegraph;
import com.prowolf.shared.Telegraph;
import com.prowolf.shared.TelegraphHandler;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends Activity {

    private Telegraph telegraph;
    private View panel;
    private View progressBar;
    private TextView attemptsText;
    private GestureDetector gestureDetector;
    private SharedPreferences settings;

    private final int MAX_ATTEMPTS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        panel = findViewById(R.id.panel);
        progressBar = findViewById(R.id.progressBar);
        attemptsText = findViewById(R.id.textView);

        gestureDetector = new GestureDetector(getApplicationContext(), new GestureListener(getApplicationContext().getResources().getDisplayMetrics()) {
            @Override
            public void onQuickFling() {
                Intent intentMain = new Intent(MainActivity.this , SettingsActivity.class);
                MainActivity.this.startActivity(intentMain);
            }
        }, new Handler(getApplicationContext().getMainLooper()));

        panel.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    telegraph.setOutgoing(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    telegraph.setOutgoing(false);
                    view.performClick();
                }
                return true;
            }
        });

        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        telegraph = new IPTelegraph(new MobileTelegraphHandler(panel, vibrator, toneGenerator, settings, getApplicationContext().getResources()), (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)) {
            @Override
            protected boolean onAttemptConnect(int attempt) {
                attemptsText.setText(String.format(getResources().getString(R.string.attempts_text_placeholder), attempt + 1, MAX_ATTEMPTS));
                return attempt < MAX_ATTEMPTS;
            }
        };

        connect();
    }

    protected void onPause() {
        super.onPause();
        telegraph.disconnect();
    }

    protected void onResume(){
        super.onResume();
        connect();
    }

    private void connect() {
        attemptsText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        try {
            URI host = new URI(settings.getString("host", "tcp://127.0.0.1:8484"));
            if (telegraph.connect(host)) {
                attemptsText.setVisibility(View.INVISIBLE);
            } else {
                attemptsText.setText(getResources().getString(R.string.attempts_failed));
            }
            progressBar.setVisibility(View.INVISIBLE);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}
