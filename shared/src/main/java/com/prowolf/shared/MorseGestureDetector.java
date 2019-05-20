package com.prowolf.shared;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

public class MorseGestureDetector implements OnGestureListener {

    public boolean shouldSettings = false;
    private DisplayMetrics displayMetrics;

    public MorseGestureDetector(DisplayMetrics displayMetrics) {
        this.displayMetrics = displayMetrics;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        float x = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 500, displayMetrics);
        if (Math.sqrt(velocityX * velocityX + velocityY * velocityY) >= x)
            shouldSettings = true;
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        return true;
    }
}
