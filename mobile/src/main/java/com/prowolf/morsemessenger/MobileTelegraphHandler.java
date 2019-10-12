package com.prowolf.morsemessenger;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.ToneGenerator;
import android.os.Vibrator;
import android.view.View;

import com.prowolf.shared.TelegraphHandler;

public class MobileTelegraphHandler implements TelegraphHandler {

    private View panel;
    private Vibrator vibrator;
    private ToneGenerator toneGenerator;
    private SharedPreferences settings;
    private Resources resources;

    private static final long[] pattern = {0, 1000};

    public MobileTelegraphHandler(View panel, Vibrator vibrator, ToneGenerator toneGenerator, SharedPreferences settings, Resources resources) {
        this.panel = panel;
        this.vibrator = vibrator;
        this.toneGenerator = toneGenerator;
        this.settings = settings;
        this.resources = resources;
    }

    @Override
    public void onIncoming(boolean incoming) {

    }

    @Override
    public void onOutgoing(boolean outgoing) {
        panel.setBackgroundColor(resources.getColor(outgoing ? android.R.color.white : android.R.color.black));
        if (outgoing) {
            vibrator.vibrate(pattern, 0);
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_DIAL_TONE_LITE);
        } else {
            vibrator.cancel();
            toneGenerator.stopTone();
        }
    }
}
