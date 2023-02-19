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

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * Demonstrate rotating Two Views using  ObjectAnimators (cube look)
 * Using rotation and translation  (R & T).  See ActivityObjAnimListR for alternate look.
 *
 * @author Dennis Lang (LanDen Labs)
 * @see <a href="http://landenlabs.com/android/index-m.html"> author's web-site </a>
 */
public class ActivityObjAnimListRT extends Activity {

    // ---- Timer ----
    private Handler m_handler = new Handler();
    private int mDurationMsec = 3000;
    private Runnable m_updateElapsedTimeTask = new Runnable() {
        public void run() {
            animateIt();
            m_handler.postDelayed(this, mDurationMsec);   // Re-execute after msec.
        }
    };

    private final List<String> mListStrings = Arrays.asList("Apple", "Avocado", "Banana",
        "Blueberry", "Coconut", "Durian", "Guava", "Kiwifruit",
        "Jackfruit", "Mango", "Olive", "Pear", "Sugar-apple");

    // ---- Local Data ----
    private int mCurrentIdx = 0;
    private TextView mTitle1;
    private TextView mTitle2;
    private ListView mListView;

    // ---- Local data ----
    private final TypeEvaluator<Float> mFloatEval = new FloatEvaluator();
    private final TypeEvaluator<Integer> mIntEval = new IntEvaluator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.obj_anim_list);
        setup();
        m_handler.postDelayed(m_updateElapsedTimeTask, mDurationMsec);
    }

    public void setup() {
        mTitle1 = Ui.viewById(this, R.id.title1);
        mTitle2 = Ui.viewById(this, R.id.title2);

        mListView = Ui.viewById(this, R.id.listview);
        /*  R.layout.list_row is ListView replacement for android.R.layout.simple_list_item_1
              because  ListView's  android:listSelector="@drawable/round_border_sel"
              fails to set state. Moving selector into list_row solves the problem.
              See advance()
        */
        mListView.setAdapter(new ArrayAdapter<>(this, R.layout.list_row, mListStrings));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mCurrentIdx = position;
            }
        });
    }

    /**
     * Start animation.
     */
    public void animateIt() {
        advance();

        // Compute begin and end angle (degrees).
        final float END_ANGLE = 90.0f;
        float beg1 = 0;
        float beg2 = END_ANGLE;
        float rot = -END_ANGLE;

        final float pivotPos = 0.5f;
        mTitle1.setPivotX(mTitle1.getWidth() * pivotPos);
        mTitle2.setPivotX(mTitle2.getWidth() * pivotPos);
        mTitle1.setPivotY(0);
        mTitle2.setPivotY(mTitle2.getHeight());

        // Build AnimatorSet to run all four animations in parallel.
        AnimatorSet animatorSet = new AnimatorSet();
        String rotParm = "RotationX";
        String tranParm = "TranslationY";
        animatorSet
                .play(ObjectAnimator.ofObject(mTitle1, rotParm, mFloatEval, beg1, beg1 + rot).setDuration(mDurationMsec))
                .with(ObjectAnimator.ofObject(mTitle1, tranParm, mIntEval, 0, mTitle1.getHeight()).setDuration(mDurationMsec))
                .with(ObjectAnimator.ofObject(mTitle2, rotParm, mFloatEval, beg2, beg2 + rot).setDuration(mDurationMsec))
                .with(ObjectAnimator.ofObject(mTitle2, tranParm, mIntEval, -mTitle2.getHeight(), 0).setDuration(mDurationMsec));
        animatorSet.start();
    }

    /**
     * Advance to next panel pair to animate, set their text.
     */
    private void advance() {
        // Swap views
        TextView title1 = mTitle1;
        mTitle1 = mTitle2;
        mTitle2 = title1;

        // Using custom ListView Adapter layout to provide a selector inside
        //   row layout so view has persistent checked (or active) state.
        //   Note: If object is not checkable (like TextView) it sets its active state.
        //   so selector should fire on state_checked or state_activated.
        mListView.setItemChecked(mCurrentIdx, true);

        // Make sure selected item is in view.
        mListView.setSelection(mCurrentIdx);

        // Advance and set data in views.
        mTitle1.setText(mListStrings.get(mCurrentIdx));
        mCurrentIdx = (mCurrentIdx + 1) % mListStrings.size();
        mTitle2.setText(mListStrings.get(mCurrentIdx));
    }

    /**
     * Interpolate angle
     */
    public class FloatEvaluator implements TypeEvaluator<Float> {
        public Float evaluate(float fraction, Float startValue,  Float endValue) {
            return startValue + ((endValue - startValue) * fraction);
        }
    }

    /**
     * Interpolate translation
     */
    public class IntEvaluator implements TypeEvaluator<Integer> {
        public Integer evaluate(float fraction, Integer startValue,  Integer endValue) {
            float numF = startValue + ((endValue - startValue) * fraction);
            return (int)numF;
        }
    }
}
