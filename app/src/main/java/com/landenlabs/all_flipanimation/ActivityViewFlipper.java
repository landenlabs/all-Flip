package com.landenlabs.all_flipanimation;

/**
 * Copyright (c) 2012 Ephraim Tekle genzeb@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
 * associated documentation files (the "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the 
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial 
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN 
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Ephraim A. Tekle (original author)
 * @author Dennis Lang  (re-written 3/21/2015)
 * @see http://landenlabs.com
 *
 */

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.landenlabs.all_flipanimation.ViewFlipFactory.FlipDirection;

/**
 * Demonstrate rotating View animation using ViewAnimator and ViewFlipper
 *
 * @see <a href="https://github.com/genzeb/flip"> author's web-site </a>
 */
public class ActivityViewFlipper extends Activity {

    // ---- Local data ----
    boolean mAutoMode = false;
    boolean mRotateYaxis = true;
    FlipDirection mDir = mRotateYaxis ? FlipDirection.LEFT_RIGHT : FlipDirection.TOP_BOTTOM;
    float[] mCameraPos = {0.0f, 0.0f, -8.0f};
    ViewAnimator mViewAnimator;
    View mClickView;
    TextView mTitle;

    // ---- Timer ----
    private Handler m_handler = new Handler();
    private int mDurationMsec = 3000;
    private Runnable m_updateElapsedTimeTask = new Runnable() {
        public void run() {
            animateIt();
            m_handler.postDelayed(this, mDurationMsec);   // Re-execute every mDurationMsec
        }
    };

    /**
     * Create flip activity which uses ViewFlipper and ViewAnimator.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_flipper);

        mViewAnimator = Ui.viewById(this, R.id.viewFlipper);
        mTitle = Ui.viewById(this, R.id.title);

        mClickView = this.findViewById(R.id.click_view);
        mClickView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                animateIt();
            }
        });

        setupUI();
    }

    /**
     * Start animation.
     */
    private void animateIt() {
        ObjectAnimator.ofFloat(mClickView, View.ALPHA, mClickView.getAlpha(), 0).start();
        mDir = ViewFlipFactory.flipTransition(mViewAnimator, mDir, mDurationMsec, mCameraPos);
    }

    /**
     * Build User Interface - setup callbacks.
     */
    private void setupUI() {
        final SlideBar seekspeedSB = new SlideBar(this.findViewById(R.id.seekSpeed), "Delay:");
        seekspeedSB.setValueChanged(new SlideBar.ValueChanged() {
            @Override
            public float onValueChanged(View v, float value) {
                mDurationMsec = (int) (value = 100 + value * 100);
                mTitle.setText(String.format("Delay:%d cameraZ:%.0f", mDurationMsec, mCameraPos[2]));
                return value;
            }
        });

        final SlideBar cameraYposSB = new SlideBar(this.findViewById(R.id.cameraYpos), "CameraY:");
        cameraYposSB.setValueChanged(new SlideBar.ValueChanged() {
            @Override
            public float onValueChanged(View v, float value) {
                mCameraPos[1] = value = (50 - value) / 10.0f;
                mTitle.setText(String.format("Delay:%d cameraY:%.0f", mDurationMsec, value));
                return value;
            }
        });
        final SlideBar cameraZposSB = new SlideBar(this.findViewById(R.id.cameraZpos), "CameraZ:");
        cameraZposSB.setValueChanged(new SlideBar.ValueChanged() {
            @Override
            public float onValueChanged(View v, float value) {
                mCameraPos[2] = value = -value / 2.0f;
                mTitle.setText(String.format("Delay:%d  cameraZ:%.0f", mDurationMsec, value));
                return value;
            }
        });

        final CheckBox autoFlipCb = Ui.viewById(this, R.id.autoflip);
        mAutoMode = autoFlipCb.isChecked();
        autoFlipCb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle auto flip checkbox to run continuous flip animations.
                mAutoMode = ((CheckBox) v).isChecked();
                if (mAutoMode) {
                    animateIt();
                    m_handler.postDelayed(m_updateElapsedTimeTask, mDurationMsec);
                } else {
                    // mAnimatorSet.cancel();
                    m_handler.removeCallbacks(m_updateElapsedTimeTask);
                }
            }
        });

        final CheckBox yaxisCb = Ui.viewById(this, R.id.yaxis);
        mRotateYaxis = yaxisCb.isChecked();
        mDir = mRotateYaxis ? FlipDirection.LEFT_RIGHT : FlipDirection.TOP_BOTTOM;
        yaxisCb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean autoMode = mAutoMode;
                if (autoMode)
                    autoFlipCb.performClick();   // Stop automatic animation.
                mRotateYaxis = ((CheckBox) v).isChecked();
                mDir = mRotateYaxis ? FlipDirection.LEFT_RIGHT : FlipDirection.TOP_BOTTOM;
                if (autoMode)
                    autoFlipCb.performClick(); // Restart automatic animation.
            }
        });
    }
}
