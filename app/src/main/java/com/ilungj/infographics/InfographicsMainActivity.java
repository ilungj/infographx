package com.ilungj.infographics;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class InfographicsMainActivity extends AppCompatActivity {

    private InfographicsGraphView mInfographicsGraphView;
    private Handler mHandler;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if(mInfographicsGraphView.startAnimation())
                mHandler.postDelayed(mRunnable, Constants.DURATION);
            else
                mHandler.removeCallbacks(this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInfographicsGraphView = (InfographicsGraphView) findViewById(R.id.graph_view);

        mHandler = new Handler();
        mRunnable.run();

    }

    /**
     * 1) Calculate largest value from the list of entries.
     * 2) Set that value as the maximum value.
     * 3) Calculate the percentage values of each entries by dividing their value by the maximum value.
     * 4) For each view of the entries, set the animation to their respective percentage values.
     *
     */

}
