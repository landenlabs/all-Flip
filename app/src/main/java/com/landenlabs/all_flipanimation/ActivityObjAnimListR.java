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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * Demonstrate rotating Two Views using  ObjectAnimators (slide look)
 * Using rotation (R) only.   See ActivityObjAnimListRT for alternate look.
 *
 * @author Dennis Lang (LanDen Labs)
 * @see <a href="https://landenlabs.com/android/index-m.html"> author's web-site </a>
 */
public class ActivityObjAnimListR extends Activity {  // ActionBarActivity

    // ---- Timer ----
    private final Handler m_handler = new Handler(Looper.getMainLooper());
    private final int mDurationMsec = 3000;
    private final Runnable m_updateElapsedTimeTask = new Runnable() {
        public void run() {
            animateIt();
            m_handler.postDelayed(this, mDurationMsec);   // Re-execute after msec.
        }
    };

    private final List<String> mListStrings = Arrays.asList("Apple", "Avocado", "Banana",
            "Blueberry", "Coconut", "Durian", "Guava", "Kiwifruit",
            "Jackfruit", "Mango", "Olive", "Pear", "Sugar-apple");

    // ---- Local Data ----
    private final float mCameraDist = 192000;
    private int mCurrentIdx = 0;
    private TextView mTitle1;
    private TextView mTitle2;
    private ListView mListView;

    // ---- Local data ----
    private static final float END_ANGLE = 90.0f;
    private final TypeEvaluator<Float> mAngleSync = new FloatEvaluator();

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

        mTitle1.setCameraDistance(mCameraDist);
        mTitle2.setCameraDistance(mCameraDist);

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
                // String itemStr = listView.getItemAtPosition(position).toString();
                // title.setText(itemStr);
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
        float beg1 = 0;
        float beg2 = -END_ANGLE;
        float rot = END_ANGLE;

        final float pivotPos = 0.5f;
        mTitle2.setPivotY(mTitle2.getHeight());
        mTitle1.setPivotY(0);
        mTitle1.setPivotX(mTitle1.getWidth() * pivotPos);
        mTitle2.setPivotX(mTitle2.getWidth() * pivotPos);

        mTitle1.setRotationX(beg1);
        mTitle2.setRotationX(beg2);

        // Build AnimatorSet to run all four animations in parallel.
        AnimatorSet animatorSet = new AnimatorSet();
        String parmStr = "RotationX";
        animatorSet
                .play(ObjectAnimator.ofObject(mTitle1, parmStr, mAngleSync, beg1, beg1 + rot).setDuration(mDurationMsec))
                .with(ObjectAnimator.ofObject(mTitle2, parmStr, mAngleSync, beg2, beg2 + rot).setDuration(mDurationMsec));
        animatorSet.start();
    }

    /**
     * Advance to next panel pair to animate.
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
     * Modify angle so both edges are in sync.
     */
    public class FloatEvaluator implements TypeEvaluator<Float> {
        public Float evaluate(float fraction, Float startValue,  Float endValue) {

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

            return startValue + ((endValue - startValue) * percent);
        }
    }
}
