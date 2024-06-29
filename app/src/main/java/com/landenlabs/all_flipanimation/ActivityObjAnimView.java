/*
 * Copyright (c) 2020 Dennis Lang (LanDen Labs) landenlabs@gmail.com
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
 * @author Dennis Lang
 * @see https://landenlabs.com/
 */

package com.landenlabs.all_flipanimation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Demonstrate rotating View animation using two ObjectAnimators.
 *
 * @author Dennis Lang (LanDen Labs)
 * @see <a href="https://landenlabs.com/android/index-m.html"> author's web-site </a>
 */
public class ActivityObjAnimView extends Activity {

    // ---- Local data ----
    private static final float END_ANGLE = 90.0f;
    private final TypeEvaluator<Float> mAngleSync = new FloatEvaluator();

    // ---- Timer ----
    private final Handler m_handler = new Handler(Looper.getMainLooper());
    private int mDurationMsec = 3000;
    private final Runnable m_updateElapsedTimeTask = new Runnable() {
        public void run() {
            animateIt();
            m_handler.postDelayed(this, mDurationMsec);   // Re-execute after msec.
        }
    };

    // ---- Layout members ----
    private TextView mTitle;
    private View[] mViews;
    private CheckBox mAutoFlipCb;
    private CheckBox mYaxisCb;
    private SlideBar mManualPosSb;
    private SlideBar mCameraDistSb;
    private View mClickView;

    // ---- Local Data ----
    private float mCameraDist = 192000;
    private final float mPivotPos = 0.5f;
    private boolean mRotateYaxis = false;
    private boolean mAutoMode = false;
    private boolean mIsForward = true;
    private AnimatorSet mAnimatorSet = new AnimatorSet();
    private int mCurrentIdx = 0;
    private int mNextIdx = 0;
    private View mView1;
    private View mView2;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.object_animator);

        mTitle = Ui.viewById(this, R.id.title);
        mViews = new View[3];
        mViews[0] = Ui.viewById(this, R.id.view1); // Red   - Hello World
        mViews[1] = Ui.viewById(this, R.id.view2); // Green - Time 4 Fun
        mViews[2] = Ui.viewById(this, R.id.view3); // Blue - Good Bye

        // Rotate non-first panels out of view.
        for (int idx = 1; idx != mViews.length; idx++)
            mViews[idx].setRotationX(90);

        advance();      // Get views loaded
        mNextIdx = 0;   // Start back at first

        setupUI();
    }

    /**
     * Execute manual animation.
     *
     * @param fract factional percent of animation, range [0..1]
     */
    public void manualAnimation(float fract) {

        // Compute begin and end angle (degrees).
        int dir = mIsForward ? 1 : -1;
        float beg1 = 0;
        float beg2 = -END_ANGLE * dir;
        float rot = END_ANGLE * dir;

        setPivotAndCamera();

        float deg1 = mAngleSync.evaluate(fract, beg1, beg1 + rot);
        float deg2 = mAngleSync.evaluate(fract, beg2, beg2 + rot);

        if (mRotateYaxis) {
            mView1.setRotationY(deg1);
            mView2.setRotationY(deg2);
        } else {
            mView1.setRotationX(deg1);
            mView2.setRotationX(deg2);
        }

        mTitle.setText(String.format("Frac:%.2f  D1:%.0f  D2:%.0f", fract, deg1, deg2));
    }

    /**
     * Start animation.
     */
    public void animateIt() {
        ObjectAnimator.ofFloat(mClickView, View.ALPHA, mClickView.getAlpha(), 0).start();

        advance();
        manualAnimation(0);

        // Compute begin and end angle (degrees).
        int dir = mIsForward ? 1 : -1;
        float beg1 = 0;
        float beg2 = -END_ANGLE * dir;
        float rot = END_ANGLE * dir;

        // Clear left over state by creating new AnimatorSet
        mAnimatorSet = new AnimatorSet();

        String parmStr = mRotateYaxis ? "RotationY" : "RotationX";
        mAnimatorSet
            .play(ObjectAnimator.ofObject(mView1, parmStr, mAngleSync, beg1, beg1 + rot).setDuration(mDurationMsec))
            .with(ObjectAnimator.ofObject(mView2, parmStr, mAngleSync, beg2, beg2 + rot).setDuration(mDurationMsec));
        mAnimatorSet.start();
    }

    /**
     * Advance to next panel pair to animate.
     */
    private void advance() {
        int dir = mIsForward ? 1 : -1;
        mCurrentIdx = mNextIdx;
        mNextIdx = mCurrentIdx + dir;

        if (mNextIdx == mViews.length) {
            mIsForward = false;
            mNextIdx = mViews.length - 2;
        } else if (mNextIdx < 0) {
            mIsForward = true;
            mNextIdx = 1;
        }

        mView1 = mViews[mCurrentIdx];
        mView2 = mViews[mNextIdx];
    }

    /**
     * Reset rotation angles on ALL views.
     */
    private void resetRotation() {
        for (View view : mViews) {
            if (mRotateYaxis) {
                view.setRotationX(0);
                view.setRotationY(90);
            } else {
                view.setRotationY(0);
                view.setRotationX(90);
            }
        }

        manualAnimation(0);
        setPivotAndCamera();
    }

    /**
     * Set pivot point for rotation.
     */
    public void setPivotAndCamera() {
        float topLeft = 0.0001f;        // Bug - pivot ignore if set to pure 0.
        // Set pivot edge for animation flip.
        if (mRotateYaxis) {
            if (mIsForward) {
                mView1.setPivotX(mView1.getWidth());
                mView2.setPivotX(topLeft);
            } else {
                mView2.setPivotX(mView2.getWidth());
                mView1.setPivotX(topLeft);
            }

            mView1.setPivotY(mView1.getHeight() * mPivotPos);
            mView2.setPivotY(mView2.getHeight() * mPivotPos);
            mView2.setRotationX(0);
            mView2.setRotationY(90);    // rotate 2nd out of view.
        } else {
            if (mIsForward) {
                mView2.setPivotY(mView2.getHeight());
                mView1.setPivotY(topLeft);
            } else {
                mView1.setPivotY(mView1.getHeight());
                mView2.setPivotY(topLeft);
            }

            mView1.setPivotX(mView1.getWidth() * mPivotPos);
            mView2.setPivotX(mView2.getWidth() * mPivotPos);
            mView2.setRotationX(90);    // rotate 2nd out of view.
            mView2.setRotationY(0);
        }

        // Pull camera far away to improve look and reduce distortion.
        mView1.setCameraDistance(mCameraDist);
        mView2.setCameraDistance(mCameraDist);
    }

    /**
     * Modify angle so both edges are in sync.
     */
    public class FloatEvaluator implements TypeEvaluator<Float> {
        public Float evaluate(float fraction,
            Float startValue,
            Float endValue) {

            // Assume non-zero start is a reverse animation..
            if (startValue != 0)
                fraction = 1 - fraction;

            // Output angle which will produce identical ending point for
            // the opposite edge of the pivoting view, assumes both views
            // are identical size.
            float angle = (float) (Math.acos(1 - fraction) * 180 / Math.PI);
            float percent = angle / END_ANGLE;

            if (startValue != 0)
                percent = 1 - percent;

            float degrees2 = startValue + ((endValue - startValue) * percent);
            return degrees2;
        }
    }

    /**
     * Build User Interface - setup callbacks.
     */
    private void setupUI() {
        mAutoFlipCb = Ui.viewById(this, R.id.autoflip);
        mYaxisCb = Ui.viewById(this, R.id.yaxis);

        // Click on panel runs one flip animation.
        mClickView = Ui.viewById(this, R.id.click_view);
        mClickView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mAutoMode) {
                    animateIt();
                }
            }
        });

        // Toggle auto flip checkbox to run continuous flip animations.
        mAutoFlipCb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAutoMode = ((CheckBox) v).isChecked();
                mManualPosSb.setEnabled(!mAutoMode);
                if (mAutoMode) {
                    animateIt();
                    m_handler.postDelayed(m_updateElapsedTimeTask, mDurationMsec);
                } else {
                    mAnimatorSet.cancel();
                    m_handler.removeCallbacks(m_updateElapsedTimeTask);
                }
            }
        });

        mYaxisCb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mRotateYaxis = ((CheckBox) v).isChecked();

                boolean autoMode = mAutoMode;
                if (autoMode)
                    mAutoFlipCb.performClick();   // Stop automatic animation.

                // Reset rotations when changing pivot axis.
                resetRotation();

                if (autoMode)
                    mAutoFlipCb.performClick(); // Restart automatic animation.
            }
        });

        final SlideBar seekspeedSB = new SlideBar(this.findViewById(R.id.seekSpeed), "Delay:");
        seekspeedSB.setValueChanged(new SlideBar.ValueChanged() {
            @Override
            public float onValueChanged(View v, float value) {
                mDurationMsec = (int) (value = 100 + value * 100);
                mTitle.setText(String.format("Delay:%d Distance:%.0f", mDurationMsec, mCameraDist));
                return value;
            }
        });

        // Execute manual animation passing seekbar as fraction position (0..1)
        mManualPosSb = new SlideBar(this.findViewById(R.id.manualPos), "Manual:");
        mManualPosSb.setValueChanged(new SlideBar.ValueChanged() {
            @Override
            public float onValueChanged(View v, float value) {
                if (!mAutoMode) {
                    manualAnimation(value / 100.0f);
                }
                return value;
            }
        });


        // Get camera distance use to set view objects camera distance.
        // Close distances cause unusual distortion and overlapping animation.
        mCameraDistSb = new SlideBar(this.findViewById(R.id.camaraDist), "Dist:");
        mCameraDistSb.setValueChanged(new SlideBar.ValueChanged() {
            @Override
            public float onValueChanged(View v, float value) {
                mCameraDist = value = 1000 + value * 1000;
                if (!mAutoMode) {
                    manualAnimation(mManualPosSb.getProgress() / 100.0f);
                }
                mTitle.setText(String.format("Delay:%d Distance:%.0f", mDurationMsec, mCameraDist));
                return value;
            }
        });


        mRotateYaxis = mYaxisCb.isChecked();
        mCameraDist = 1000 + mCameraDistSb.getProgress() * 1000;
        mAutoMode = mAutoFlipCb.isChecked();
        mManualPosSb.setEnabled(!mAutoMode);
        mManualPosSb.setProgress(0);

        resetRotation();
    }
}