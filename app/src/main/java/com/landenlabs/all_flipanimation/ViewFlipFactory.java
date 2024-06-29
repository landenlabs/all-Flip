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

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ViewAnimator;

/**
 * This class contains methods for creating {@link Animation} objects for some of
 * the most common animation, including a 3D flip animation, {@link FlipAnimation}.
 */
public class ViewFlipFactory {

    /**
     * Flip to the next view of the {@code ViewAnimator}'s subviews. A call to this method will initiate a {@link FlipAnimation} to show the next View.
     * If the currently visible view is the last view, flip direction will be reversed for this transition.
     *
     * @param viewAnimator the {@code ViewAnimator}
     * @param dir          the direction of flip
     * @param duration     the transition duration in milliseconds
     * @return direction  hit end - flips direction.
     */
    public static FlipDirection flipTransition(final ViewAnimator viewAnimator, FlipDirection dir, long duration, float[] cameraPos) {

        final int currentIndex = viewAnimator.getDisplayedChild();
        final int nextIndex = (currentIndex + 1) % viewAnimator.getChildCount();

        final View fromView = viewAnimator.getCurrentView();
        final View toView = viewAnimator.getChildAt(nextIndex);

        Animation[] animc = flipAnimation(fromView, toView, dir, duration, new LinearInterpolator(), cameraPos);

        viewAnimator.setOutAnimation(animc[0]);
        viewAnimator.setInAnimation(animc[1]);

        viewAnimator.showNext();

        return (nextIndex < currentIndex) ? dir.theOtherDirection() : dir;
    }

    /**
     * Create a pair of {@link FlipAnimation} that can be used to flip 3D transition from {@code fromView} to {@code toView}.
     * A typical use case is with {@link ViewAnimator} as an out and in transition.
     * <p/>
     * NOTE: Avoid using this method. Instead, use {@link #flipTransition}.
     *
     * @param fromView     the view transition away from
     * @param toView       the view transition to
     * @param dir          the flip direction
     * @param duration     the transition duration in milliseconds
     * @param interpolator the interpolator to use (pass {@code null} to use the {@link AccelerateInterpolator} interpolator)
     * @return animation pair
     */
    public static Animation[] flipAnimation(final View fromView, final View toView, FlipDirection dir,
        long duration, Interpolator interpolator, float[] cameraPos) {

        int dirSign = dir.getDirSign();
        int axis;
        float[] inRotation, outRotation;
        float[] inOriginF, outOriginF;
        float[] inPivotF, outPivotF;

        if (dir == FlipDirection.BOTTOM_TOP || dir == FlipDirection.TOP_BOTTOM) {
            axis = FlipAnimation.ROTATION_X;
            inRotation = new float[]{0, -90};
            outRotation = new float[]{90, 0};
            inOriginF = new float[]{0, 0};
            outOriginF = new float[]{0, -1};
            inPivotF = new float[]{0.5f, 0};
            outPivotF = new float[]{0.5f, 1};
        } else {
            axis = FlipAnimation.ROTATION_Y;
            inRotation = new float[]{0, 90};
            outRotation = new float[]{-90, 0};
            inOriginF = new float[]{0, 0};
            outOriginF = new float[]{-1, 0};
            inPivotF = new float[]{0, 0.5f};
            outPivotF = new float[]{1, 0.5f};
        }

        Animation[] result = new Animation[2];

        FlipAnimation outFlip = new FlipAnimation(
            inRotation,     // Rotation from -> to
            inOriginF,      // Origin (fraction of view dimensions)
            inPivotF,       // Pivot (fraction of view dimensions)
            cameraPos, axis, dirSign, fromView);
        outFlip.setDuration(duration);
        outFlip.setFillAfter(true);
        outFlip.setInterpolator(interpolator);
        result[0] = outFlip;

        FlipAnimation inFlip = new FlipAnimation(
            outRotation,    // Rotation from -> to
            outOriginF,     // Origin (fraction of view dimensions)
            outPivotF,      // Pivot (fraction of view dimensions)
            cameraPos, axis, dirSign, toView);
        inFlip.setDuration(duration);
        inFlip.setFillAfter(true);
        inFlip.setInterpolator(interpolator);
        result[1] = inFlip;

        return result;
    }

    /**
     * Enum defines flip view transitions:
     * <p/> LEFT_RIGHT, RIGHT_LEFT, TOP_BOTTOM, BOTTOM_TOP
     * <p/> FlipDirection is used during the creation of {@link FlipAnimation} animations.
     */
    public  enum FlipDirection {

        LEFT_RIGHT(0),
        RIGHT_LEFT(1),
        TOP_BOTTOM(0),
        BOTTOM_TOP(1);

        private final int mDir;

        FlipDirection(int d) {
            mDir = d;
        }

        public int getDirSign() {
            return (mDir == 0) ? 1 : -1;
        }

        public FlipDirection theOtherDirection() {
            switch (this) {
                case LEFT_RIGHT:
                    return RIGHT_LEFT;
                case TOP_BOTTOM:
                    return BOTTOM_TOP;
                case RIGHT_LEFT:
                    return LEFT_RIGHT;
                case BOTTOM_TOP:
                default:
                    return TOP_BOTTOM;
            }
        }
    }

    /**
     * This class extends Animation to support a 3D flip view transition animation. Two instances of this class is
     * required: one for the "from" view and another for the "to" view.
     * <p/>
     * NOTE: use {@link ViewFlipFactory} to use this class.
     */
    public static class FlipAnimation extends Animation {
        public static final int ROTATION_X = 0;
        public static final int ROTATION_Y = 1;

        private final float mFromDegrees;
        private final float mToDegrees;
        private final float mTransXf;
        private final float mTransYf;
        private final float mPivotXf;
        private final float mPivotYf;

        enum Mode {USE_CAMERA, VIEW_ONLY}
        final Mode mMode = Mode.USE_CAMERA;

        private Camera mCamera;
        private final float[] mCameraPos;
        private final int mAxis;
        private final int mDir;   // 1=left To Right, -1=right to left
        private final View mView;

        /**
         * Constructs a new {@code FlipAnimation} object.Two {@code FlipAnimation} objects are needed for a complete transition b/n two views.
         *
         * @param fromToDegrees From, To angles in degrees for a rotation along direction axis.
         * @param originF       x,y axis  translation (fraction of view dimensions)
         * @param pivotF        x,y axis  pivot for rotation (fraction of view dimensions)
         * @param cameraPos     Camera viewing position x,y,z
         * @param axis          Rotation axis X or Y
         * @param dir           Direction 1=left-to-right or -1=right-to-left
         * @param view          View object being transformed.
         */
        public FlipAnimation(
            float[] fromToDegrees,
            float[] originF,
            float[] pivotF,
            float[] cameraPos,
            int axis, int dir,
            View view) {
            mDir = dir;
            mFromDegrees = fromToDegrees[0] * dir;
            mToDegrees = fromToDegrees[1] * dir;
            mTransXf = originF[0];
            mTransYf = originF[1];
            mPivotXf = flip(pivotF[0]);
            mPivotYf = flip(pivotF[1]);
            mCameraPos = cameraPos;
            mAxis = axis;
            mView = view;

            // Log.e("flip", String.format("From:%.0f To:%.0f PivXf:%.1f ",mFromDegrees, mToDegrees, mPivotXf) + ((TextView)view).getText());
        }

        /**
         * Flip edge (ex: 0->1  1->0 0.5 -> 0.5) with mDir set to 1 or -1
         *
         * @param value to return or flip.
         * @return input value oriented inside 0..1 domain depending on mDir.
         */
        float flip(float value) {
            int n = (1 - mDir) / 2; // 0 or 1
            return value * mDir + n;
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            mCamera = new Camera();
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation trans) {

            // Two possible ways to apply transformation
            switch (mMode) {
                case USE_CAMERA:
                    // Use camera for perspective rotation
                    applyTransformationCamera(interpolatedTime, trans);
                    break;
                case VIEW_ONLY:
                    // Rotate view directly.
                    applyTransformationView(interpolatedTime, trans);
            }
        }

        /**
         * Apply Transform using Camera for 3d perspective rotation.
         *
         * @param interpolatedTime Value from interpolation [0 to 1].
         * @param trans            Holds matrix.
         */
        private void applyTransformationCamera(float interpolatedTime, Transformation trans) {
            final float fromDegrees = mFromDegrees;
            float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

            float height = mView.getHeight();
            float width = mView.getWidth();

            final Matrix matrix = trans.getMatrix();
            mCamera.save();
            mCamera.setLocation(mCameraPos[0], mCameraPos[1], mCameraPos[2]);

            if (mAxis == ROTATION_X) {
                mCamera.rotateX(degrees);
            } else {
                mCamera.rotateY(degrees);
            }

            mCamera.getMatrix(matrix);
            mCamera.restore();

            if (mAxis == ROTATION_X) {
                matrix.postTranslate(0, (interpolatedTime + mTransYf) * height * mDir);
            } else {
                matrix.postTranslate((interpolatedTime + mTransXf) * width * mDir, 0);
            }

            float pivotX = mPivotXf * width;
            float pivotY = mPivotYf * height;
            matrix.preTranslate(-pivotX, -pivotY);
            matrix.postTranslate(pivotX, pivotY);
        }

        /**
         * Apply rotation directly to View.
         *
         * @param interpolatedTime Value from interpolation [0 to 1].
         * @param trans            Holds matrix.
         */
        private void applyTransformationView(float interpolatedTime, Transformation trans) {
            final float fromDegrees = mFromDegrees;
            float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

            float height = mView.getHeight();
            float width = mView.getWidth();

            mView.setCameraDistance(1280 + mCameraPos[2] * -100);

            mView.setPivotX(mPivotXf * width);
            mView.setPivotY(mPivotYf * height);
            if (mAxis == ROTATION_X) {
                mView.setRotationX(degrees);
                mView.setTranslationY((interpolatedTime + mTransYf) * height * mDir);
            } else {
                mView.setRotationY(degrees);
                mView.setTranslationX((interpolatedTime + mTransXf) * width * mDir);
            }
        }
    }
}
