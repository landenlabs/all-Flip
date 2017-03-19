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

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.app.Activity;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Demonstrate rotating Object animation for ImageView only.
 *
 * @author Dennis Lang (LanDen Labs)
 * @see <a href="http://landenlabs.com/android/index-m.html"> author's web-site </a>
 */
public class ActivityObjAnimImg extends Activity {

    // ---- Data ----
    private static final float END_ANGLE = 90.0f;
    float mCameraZ = -50;
    Flip3dAnimation mRotation1;
    Flip3dAnimation mRotation2;

    // ---- Timer ----
    private Handler m_handler = new Handler();
    private int mDurationMsec = 3000;
    private Runnable m_updateElapsedTimeTask = new Runnable() {
        public void run() {
            animateIt();
            m_handler.postDelayed(this, mDurationMsec);   // Re-execute after msec.
        }
    };

    // ---- Layout members ----
    private TextView mTitle;
    private ImageView mView1;
    private ImageView mView2;
    private CheckBox mAutoFlipCb;
    private CheckBox mYaxisCb;
    private SlideBar mManualPosSb;
    private SlideBar mCameraDistSb;
    private View mClickView;

    // ---- Local Data ----
    private float mCameraDist = 192000;
    private float mPivotPos = 0.5f;
    private boolean mRotateYaxis = false;
    private boolean mAutoMode = false;
    private boolean mIsForward = true;
    private AnimatorSet mAnimatorSet = new AnimatorSet();


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.obj_anim_images);

        mTitle = Ui.viewById(this, R.id.title);

        mView1 = Ui.viewById(this, R.id.view1);
        mView2 = Ui.viewById(this, R.id.view2);

        // Create new 3D matrix rotation animators.
        mRotation1 = new Flip3dAnimation(mView1.getMatrix());
        mRotation2 = new Flip3dAnimation(mView2.getMatrix());

        setupUI();
        mYaxisCb.setVisibility(View.GONE);
        mCameraDistSb.setVisibility(View.GONE);

        // Wait for view to get created.
        mView1.post( new Runnable() {
            @Override public void run() {
                manualAnimation(0.1f);
            }
        });
    }

    /**
     * Execute manual animation.
     *
     * @param fract factional percent of animation, range [0..1]
     */
    public void manualAnimation(float fract) {
        setPivotAndCamera();
        mView1.setImageMatrix(mRotation1.getMatrix(fract));
        mView2.setImageMatrix(mRotation2.getMatrix(fract));
    }

    /**
     * Start animation.
     */
    public void animateIt() {
        ObjectAnimator.ofFloat(mClickView, View.ALPHA, mClickView.getAlpha(), 0).start();

        setPivotAndCamera();
        mIsForward = !mIsForward;

        // For imageMatrix to work you must also set scaleType to Matrix.
        ObjectAnimator anim1 = ObjectAnimator.ofObject(
                mView1,
                "imageMatrix",  // Property of imageView
                new MatrixEvaluator(),
                mRotation1.getMatrix(0), mRotation1.getMatrix(1));
        anim1.setDuration(mDurationMsec);

        ObjectAnimator anim2 = ObjectAnimator.ofObject(
                mView2,
                "imageMatrix",  // Property of imageView
                new MatrixEvaluator(),
                mRotation2.getMatrix(0), mRotation2.getMatrix(1));
        anim2.setDuration(mDurationMsec);

        // Execute two Object animations together.
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(anim1).with(anim2);
        animatorSet.start();
    }

    public class Flip3dAnimation {
        private final Matrix mMatrix;
        float mFromDegrees;
        float mToDegrees;
        float mCenterX;
        float mCenterY;
        float mCameraZ;
        float mScaleX, mScaleY;
        private Camera mCamera;

        public Flip3dAnimation(Matrix matrix) {
            mMatrix = matrix;
            mCamera = new Camera();
        }

        public Matrix getMatrix(float fract) {
            return applyTransformation(fract);
        }

        /**
         * Apply animation transformation (rotate matrix)
         * @param interpolatedTime
         * @return
         */
        protected Matrix applyTransformation(float interpolatedTime) {

            float degrees1 = mFromDegrees + ((mToDegrees - mFromDegrees) * interpolatedTime);
            final Camera camera = mCamera;

            Matrix matrix = new Matrix();
            matrix.set(mMatrix);

            camera.save();
            camera.setLocation(0, 0, mCameraZ);
            camera.rotateY(degrees1);

            camera.getMatrix(matrix);
            camera.restore();

            matrix.preTranslate(-mCenterX, -mCenterY);
            matrix.preScale(mScaleX, mScaleX);
            matrix.postTranslate(mCenterX, mCenterY);

            return matrix;
        }
    }

    /**
     * Reset rotation angles on ALL views.
     */
    private void resetRotation() {
        // TODO - reset matrix
        manualAnimation(0);
        setPivotAndCamera();
    }

    /**
     * Set pivot point for rotation, camera and rotation.
     */
    public void setPivotAndCamera() {
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

        float w = mView1.getDrawable().getBounds().width();
        if (w > 0) {
            mRotation1.mScaleX = mView1.getWidth() / w;
            mRotation1.mScaleY = mView1.getHeight() / w;
        }
        w = mView2.getDrawable().getBounds().width();
        if (w > 0) {
            mRotation2.mScaleX =  mView2.getWidth() / w;
            mRotation2.mScaleY =  mView2.getHeight() / w;
        }

        mRotation1.mCenterX = mView1.getWidth();
        mRotation1.mCenterY = mView1.getHeight() / 2.0f;
        mRotation1.mCameraZ = mCameraZ;

        mRotation2.mCenterX = 0.0f;
        mRotation2.mCenterY = mView2.getHeight() / 2.0f;
        mRotation2.mCameraZ = mCameraZ;
    }

    /**
     * Linear interpolation between two matrix.
     */
    public class MatrixEvaluator implements TypeEvaluator<Matrix> {
        public Matrix evaluate(float fraction,
            Matrix startValue,
            Matrix endValue) {
            float[] startEntries = new float[9];
            float[] endEntries = new float[9];
            float[] currentEntries = new float[9];

            startValue.getValues(startEntries);
            endValue.getValues(endEntries);

            for (int i = 0; i < 9; i++)
                currentEntries[i] = (1 - fraction) * startEntries[i]
                    + fraction * endEntries[i];

            Matrix matrix = new Matrix();
            matrix.setValues(currentEntries);
            return matrix;
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
                    mManualPosSb.setEnabled(false);
                    animateIt();
                    m_handler.postDelayed(m_updateElapsedTimeTask, mDurationMsec);
                } else {
                    mManualPosSb.setEnabled(true);
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