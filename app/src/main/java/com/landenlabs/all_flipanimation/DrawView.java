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
 * @see https://LanDenLabs.com/
 */

package com.landenlabs.all_flipanimation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Simple class to draw profile of swinging view panels
 */
public class DrawView extends View {

    final boolean mHing1Right = true;
    float mDegrees1 = 10.0f;
    final boolean mHing2Right = false;
    float mDegrees2 = 60.0f;

    final int mColorBg1 = Color.rgb(128, 128, 128);
    final int mColorBg2 = Color.rgb(160, 160, 160);
    float mMin = 0.1f;
    float mSize = 0.4f;
    float mMaxX = mMin + mSize;
    float mMaxY = mMin + mSize * 2;

    final Paint paint = new Paint();

    public DrawView(Context context) {
        super(context);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAngle1(float degrees) {
        mDegrees1 = degrees;
        invalidate();
    }
    public void setAngle2(float degrees) {
        mDegrees2 = degrees;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {

        // canvas.save();
        // canvas.scale(....)
        // canvas.restore();
        // int canvasDim = Math.min(canvas.getWidth(), canvas.getHeight());
        int viewDim = Math.min(this.getWidth(), this.getHeight());
        int dim = viewDim;

        mMin = 0.1f * dim;
        mSize = 0.8f * dim;
        mMaxX = mMin + mSize;
        mMaxY = mMin + mSize * 2;

        paint.setColor(mColorBg1);
        paint.setStrokeWidth(3);
        canvas.drawLine(mMin, mMin, mMin, mMaxY, paint);
        canvas.drawLine(mMaxX,mMin, mMaxX, mMaxY, paint);
        paint.setColor(mColorBg2);
        float cenY = (mMaxY+mMin)/2;
        canvas.drawLine(0,cenY, 1, cenY, paint);

        paint.setStrokeWidth(4);
        paint.setColor(0xff408080); // Color.CYAN);
        drawLine(canvas, mHing1Right, mDegrees1);

        paint.setColor(0xff804080); // Color.MAGENTA);
        drawLine(canvas, mHing2Right, mDegrees2);
    }

    private void drawLine(Canvas canvas, boolean rightHing, float degrees) {
        float cenY = (mMaxY+mMin)/2;
        degrees = rightHing ? degrees + 180 : degrees;
        float x1 = rightHing ? mMaxX : mMin;
        float y1 = cenY;
        float x2 = (float)Math.cos(Math.toRadians(degrees)) * mSize + x1;
        float y2 = (float)Math.sin(Math.toRadians(degrees)) * mSize + y1;

        canvas.drawLine(x1, y1, x2, y2, paint);

        RectF rectF = new RectF(x1-mSize, y1-mSize, x1+mSize, y1+mSize);
        paint.setAlpha(40);
        canvas.drawArc(rectF, 0, degrees, true, paint);
    }
}