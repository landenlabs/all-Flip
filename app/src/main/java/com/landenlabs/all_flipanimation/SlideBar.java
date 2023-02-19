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
 * @see http://LanDenLabs.com/
 */

package com.landenlabs.all_flipanimation;

import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by dlang_local on 3/17/2015.
 */
public class SlideBar {
    View mParent;
    TextView mLabel;
    SeekBar mSeekBar;
    String mLabelFmt;
    ValueChanged mValueChanged;

    public SlideBar(View parent, String labelFmt) {
        mParent = parent;
        mLabel = (TextView) parent.findViewById(R.id.labelbar);
        mSeekBar = (SeekBar) parent.findViewById(R.id.slidebar);
        mLabelFmt = labelFmt;

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                updateLabel(progressValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void updateLabel(float progressValue) {
        if (mValueChanged != null) {
            float value = mValueChanged.onValueChanged(mParent, progressValue);
            mLabel.setText(String.format(mLabelFmt, value));
        }
    }

    public int getProgress() {
        return mSeekBar.getProgress();
    }

    public void setProgress(int value) {
        mSeekBar.setProgress(value);
    }

    public void setValueChanged(ValueChanged valueChanged) {
        mValueChanged = valueChanged;
        updateLabel(mSeekBar.getProgress());
    }

    public interface ValueChanged {
        float onValueChanged(View parent, float value);
    }

    public void setEnabled(boolean enabled) {
        mLabel.setEnabled(enabled);
        mSeekBar.setEnabled(enabled);
    }

    public void setVisibility(int vis) {
        mLabel.setVisibility(vis);
        mSeekBar.setVisibility(vis);
    }
}
