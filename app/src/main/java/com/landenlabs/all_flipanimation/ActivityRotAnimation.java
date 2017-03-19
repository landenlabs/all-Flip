package com.landenlabs.all_flipanimation;

/**
 * Copyright (c) 2015 Dennis Lang (LanDen Labs) landenlabs@gmail.com
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
 * @author Dennis Lang  (3/21/2015)
 * @see http://landenlabs.com
 *
 */

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Demonstrate rotating View animation using two rotating animations.
 *
 * @author Dennis Lang (LanDen Labs)
 * @see <a href="http://landenlabs.com/android/index-m.html"> author's web-site </a>
 * // http://www.inter-fuser.com/2009/08/android-animations-3d-flip.html
 */
public class ActivityRotAnimation extends Activity {

    // ---- Layout ----
    View mView1;
    View mView2;
    View mClickView;
    DrawView mDrawView;
    TextView mAngle1;
    TextView mAngle2;

    // ---- Data ----
    float mCameraZ = -25;
    Flip3dAnimation mRotation1;
    Flip3dAnimation mRotation2;
    boolean mRotateYaxis = true;
    boolean mIsForward = true;
    boolean mAutoMode = false;

    MediaPlayer mSoundClick;
    MediaPlayer mSoundShut;

    // ---- Timer ----
    private Handler m_handler = new Handler();
    private int mDurationMsec = 3000;
    private Runnable m_updateElapsedTimeTask = new Runnable() {
        public void run() {
            animateIt();
            m_handler.postDelayed(this, mDurationMsec);   // Re-execute after msec
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rot_animation);

        mView1 = Ui.viewById(this, R.id.view1);
        mView2 = Ui.viewById(this, R.id.view2);

        // Create a new 3D rotation with the supplied parameter
        mRotation1 = new Flip3dAnimation();
        mRotation2 = new Flip3dAnimation();

        Ui.<TextView>viewById(this, R.id.side_title).setText("Rotating Animation");
        setupUI();
    }

    /**
     * Start animation.
     */
    public void animateIt() {
        ObjectAnimator.ofFloat(mClickView, View.ALPHA, mClickView.getAlpha(), 0).start();

        final float end = 90.0f;

        if (mIsForward) {
            mRotation1.mFromDegrees = 0.0f;
            mRotation1.mToDegrees = end;
            mRotation2.mFromDegrees = -end;
            mRotation2.mToDegrees = 0.0f;
        } else {
            mRotation1.mFromDegrees = end;
            mRotation1.mToDegrees = 0.0f;
            mRotation2.mFromDegrees = 0.0f;
            mRotation2.mToDegrees = -end;
        }

        mIsForward = !mIsForward;

        if (mRotateYaxis) {
            mRotation1.mCenterX = mView1.getWidth();
            mRotation1.mCenterY = mView1.getHeight() / 2.0f;
            mRotation2.mCenterX = 0.0f;
            mRotation2.mCenterY = mView2.getHeight() / 2.0f;
        } else {
            mRotation1.mCenterY = 0.0f; // mView1.getHeight();
            mRotation1.mCenterX = mView1.getWidth() / 2.0f;
            mRotation2.mCenterY = mView1.getHeight();   // 0.0f;
            mRotation2.mCenterX = mView2.getWidth() / 2.0f;
        }

        mRotation1.reset(mView1, mDurationMsec, mCameraZ);
        mRotation2.reset(mView2, mDurationMsec, mCameraZ);
        mRotation2.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }
            @Override public void onAnimationEnd(Animation animation) {
                mSoundShut.start();
            }
            @Override public void onAnimationRepeat(Animation animation) { }
        });

        // Run both animations in parallel.
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setInterpolator(new LinearInterpolator());
        animationSet.addAnimation(mRotation1);
        animationSet.addAnimation(mRotation2);
        animationSet.start();
    }

    public class Flip3dAnimation extends Animation {
        float mFromDegrees;
        float mToDegrees;
        float mCenterX = 0;
        float mCenterY = 0;
        float mCameraZ = -8;
        Camera mCamera;
        View mView;

        public Flip3dAnimation() {
            setFillEnabled(true);
            setFillAfter(true);
            setFillBefore(true);
        }

        public void reset(View view, int durationMsec, float cameraZ) {
            mCameraZ = cameraZ;
            setDuration(durationMsec);
            view.clearAnimation();      // This is very important to get 2nd..nth run to work.
            view.setAnimation(this);
            mView = view;
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            mCamera = new Camera();
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation trans) {
            final float fromDegrees = mFromDegrees;
            float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

            final Camera camera = mCamera;
            final Matrix matrix = trans.getMatrix();

            camera.save();
            camera.setLocation(0, 0, mCameraZ);

            if (mRotateYaxis)
                camera.rotateY(degrees);
            else
                camera.rotateX(degrees);

            camera.getMatrix(matrix);
            camera.restore();

            matrix.preTranslate(-mCenterX, -mCenterY);
            matrix.postTranslate(mCenterX, mCenterY);

            final float degree3 = degrees;
            if (mView == mView1) {
                mDrawView.setAngle1(degree3);
                mAngle1.setText(String.format("%.0f°", degree3));
            } else {
                mDrawView.setAngle2(degree3);
                mAngle2.setText(String.format("%.0f°", degree3));
            }
        }
    }

    /**
     * Build User Interface - setup callbacks.
     */
    private void setupUI() {

        mSoundClick = MediaPlayer.create(this, R.raw.click);
        // mSoundClick.setVolume(0.5f, 0.5f);
        mSoundShut = MediaPlayer.create(this, R.raw.shut);
        // mSoundShut.setVolume(0.3f, 0.3f);

        final TextView title = (TextView) this.findViewById(R.id.title);

        mClickView = this.findViewById(R.id.click_view);
        mClickView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSoundClick.start();
                animateIt();
            }
        });

        final SlideBar seekspeedSB = new SlideBar(this.findViewById(R.id.seekSpeed), "Delay:");
        seekspeedSB.setValueChanged(new SlideBar.ValueChanged() {
            @Override
            public float onValueChanged(View v, float value) {
                mDurationMsec = (int) (value = 100 + value * 100);
                title.setText(String.format("Delay:%d CameraZ:%.0f", mDurationMsec, mCameraZ));
                return value;
            }
        });

        final SlideBar cameraZpos = new SlideBar(this.findViewById(R.id.cameraZpos), "CamZ:");
        cameraZpos.setProgress((int) (mCameraZ / -2 + 50));
        cameraZpos.setValueChanged(new SlideBar.ValueChanged() {
            @Override
            public float onValueChanged(View v, float value) {
                mCameraZ = value = (50 - value) * 2.0f;
                title.setText(String.format("Delay:%d CameraZ:%.0f", mDurationMsec, mCameraZ));
                return value;
            }
        });

        final CheckBox autoFlipCb = Ui.viewById(this, R.id.autoflip);
        autoFlipCb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAutoMode = ((CheckBox) v).isChecked();
                if (mAutoMode) {
                    m_handler.postDelayed(m_updateElapsedTimeTask, 0);
                } else {
                    m_handler.removeCallbacks(m_updateElapsedTimeTask);
                }
            }
        });

        final CheckBox yaxisCb = Ui.viewById(this, R.id.yaxis);
        mRotateYaxis = yaxisCb.isChecked();
        yaxisCb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean autoMode = mAutoMode;
                if (autoMode)
                    autoFlipCb.performClick();      // Stop automatic animation.
                mRotateYaxis = ((CheckBox) v).isChecked();
                if (autoMode)
                    autoFlipCb.performClick();      // Restart automatic animation.
            }
        });

        mDrawView = Ui.viewById(this, R.id.drawView);
        mAngle1 = Ui.viewById(this, R.id.angle1);
        mAngle2 = Ui.viewById(this, R.id.angle2);
    }
}