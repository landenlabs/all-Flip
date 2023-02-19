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

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Demonstrate rotating View animation using different techniques.
 */
public class MainActivity extends Activity {

    /**
     * Create flip activity which uses ViewFlipper and ViewAnimator.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.findViewById(R.id.item5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ActivityObjAnimView.class);
            }
        });

        this.findViewById(R.id.item1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ActivityViewFlipper.class);
            }
        });

        this.findViewById(R.id.item2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ActivityRotAnimation.class);
            }
        });

        this.findViewById(R.id.item3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ActivityRotAnimComp.class);
            }
        });

        this.findViewById(R.id.item4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ActivityObjAnimImg.class);
            }
        });

        this.findViewById(R.id.objAnimListRT).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ActivityObjAnimListRT.class);
            }
        });

        this.findViewById(R.id.objAnimListR).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ActivityObjAnimListR.class);
            }
        });

        // ---- Open web site with click on titles or logo.
        this.findViewById(R.id.title1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebSite();
            }
        });
        this.findViewById(R.id.title2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebSite();
            }
        });
        this.findViewById(R.id.title3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebSite();
            }
        });
        TextView buildDateTv = this.findViewById(R.id.buildDate);
        SimpleDateFormat dateFmt = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        Date buildDate = new Date(BuildConfig.BuildTimeMilli);
        buildDateTv.setText(dateFmt.format(buildDate));
    }

    private void startActivity(Class<?> act) {
        // Play click sound
        final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.click);
        mediaPlayer.start();
        // mediaPlayer.release();

        // Start activity.
        Intent myIntent = new Intent(MainActivity.this, act);
        MainActivity.this.startActivity(myIntent);
    }

    private void openWebSite() {
        Intent intent = new Intent(Intent.ACTION_VIEW,
            Uri.parse("http://landenlabs.com/android/flip-animation/index.html"));
        startActivity(intent);
    }
}
