package com.ilungj.infographics;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.percent.PercentRelativeLayout;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Il Ung on 2/7/2017.
 */

public class InfographicsGraphView extends ScrollView {

    private Context mContext;

    private BarView mBarView[];
    private float[] mBarPosition;
    private int mBarWidthInitial;
    private int mBarHeightInitial;

    private List<LinkedList<Float>> mValueList;
    private float mValueMax;


    /**
     * Constructor.
     * @param context
     */
    public InfographicsGraphView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    /**
     * Constructor.
     * @param context
     * @param attrs
     */
    public InfographicsGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    /**
     * Constructor.
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public InfographicsGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    /**
     * Starts the animation of the view.
     * @return false if list is empty, else return true
     */
    public boolean startAnimation() {
        if(mValueList.get(0).size() == 0) {
            return false;
        }
        calculate();
        for(BarView barView : mBarView)
            barView.startAnimation();
        return true;
    }

    /**
     * Calculates the maximum value, sorts the bars by their values, and ranks them.
     * @return the maximum value in the list
     */
    private float calculate() {

        HashMap<Float, Integer> hm = new HashMap<>();
        float[] arr = new float[mValueList.size()];
        for(int i = 0; i < mValueList.size(); i++) {
            hm.put(mValueList.get(i).get(0), i);
            arr[i] = mValueList.get(i).get(0);
        }

        Arrays.sort(arr);

        for(int i = 0; i < arr.length; i++) {
            mBarView[hm.get(arr[i])].mRank = arr.length - i; // Find bar view with value of arr[i], and its rank is the value's position in the sorted array
        }

        mValueMax = arr[arr.length - 1];

        return mValueMax;
    }

    /**
     * Initializes the view with a scroll view parent, and a relative layout child with the
     * bar views.
     */
    private void init() {

        extract();

        ScrollView parent = (ScrollView) inflate(mContext, R.layout.graph_view, this);
        PercentRelativeLayout child = (PercentRelativeLayout) parent.findViewById(R.id.layout);

        // Layout parameters for the bars.
        RelativeLayout.LayoutParams[] wrapParams = new RelativeLayout.LayoutParams[Constants.NUM_OF_ENTRIES];
        for(int i = 0; i < Constants.NUM_OF_ENTRIES; i++) {
            wrapParams[i] = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Layout parameter for the relative layout graph.
        PercentRelativeLayout.LayoutParams matchParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        PercentRelativeLayout graph = new PercentRelativeLayout(mContext);
        graph.setLayoutParams(matchParams);

        ViewGroup.LayoutParams tempParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        View line = new View(mContext);
        line.setLayoutParams(tempParams);
        line.setBackgroundColor(Color.CYAN);

        // Check if json file was written correctly.
        if(mValueList.size() != Constants.NUM_OF_ENTRIES)
            Toast.makeText(mContext, "", Toast.LENGTH_SHORT).show();

        else {
            mBarView = new BarView[Constants.NUM_OF_ENTRIES];
            mBarPosition = new float[Constants.NUM_OF_ENTRIES];
            for (int i = 0; i < Constants.NUM_OF_ENTRIES; i++) {
                mBarView[i] = new BarView(mContext);
                mBarView[i].setId(i + 1);
                mBarView[i].mId = i;
                mBarView[i].mRank = i + 1;
                if (i == 0) {
                    graph.addView(mBarView[i]);
                } else {
                    wrapParams[i].addRule(RelativeLayout.BELOW, mBarView[i - 1].getId());
                    wrapParams[i].setMargins(0, 30, 0, 0);
                    graph.addView(mBarView[i], wrapParams[i]);
                }
            }

            final RelativeLayout view = graph;
            final View view2 = line;
            view.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            // Layout has happened here.
                            for(int i = 0; i < Constants.NUM_OF_ENTRIES; i++) {
                                mBarPosition[i] = view.getChildAt(i).getY();
                            }

                            mBarWidthInitial = (int) (0.75 * mBarView[1].getWidth());

                            ViewGroup.LayoutParams temp = view2.getLayoutParams();
                            temp.width = 3;
                            temp.height = view.getHeight();
//                            view2.setLayoutParams(temp);

                            // Don't forget to remove your listener when you are done with it.
                            view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });

            child.addView(line);
            child.addView(graph);

//            if(line == null)
//                Log.d("TEST5", "line is null");
//            ViewGroup.LayoutParams temp = line.getLayoutParams();
        }

    }

    private void extract() {

        Gson gson = new Gson();
        String json = null;
        try {
            InputStream is = mContext.getAssets().open("json.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        GsonObject gsonObject = gson.fromJson(json, GsonObject.class);
        mValueList = gsonObject.mList;

    }

    private class BarView extends RelativeLayout {

        private Context mContext;
        private int mRank;
        private int mId;

        private View mBar;
        private TextView mText;

        private float mPositionY;

        public BarView(Context context) {
            super(context);
            this.mContext = context;
            init();
        }

        public BarView(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.mContext = context;
            init();
        }

        public BarView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            this.mContext = context;
            init();
        }

        private void startAnimation() {
            if(mValueList.get(mId).get(0) != 1)
                mText.setText(String.format(Locale.US, "%.0f", mValueList.get(mId).get(0)));

            ValueAnimator scaleAnimation = ValueAnimator.ofInt(mBar.getWidth(), (int) (mBarWidthInitial * (mValueList.get(mId).get(0) / mValueMax)));
            scaleAnimation.setDuration(Constants.DURATION);
            scaleAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    int val = (Integer) animation.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = mBar.getLayoutParams();
                    layoutParams.width = val;
                    mBar.setLayoutParams(layoutParams);
                }
            });
            scaleAnimation.start();

            final View view = this;
            ValueAnimator translateAnimation = ValueAnimator.ofFloat(view.getY(), mBarPosition[mRank - 1]);
            translateAnimation.setDuration(Constants.DURATION);
            translateAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float val = (Float) animation.getAnimatedValue();
                    view.setY(val);
                }
            });
            translateAnimation.start();
            mValueList.get(mId).remove(0);
        }

        private void init() {
            inflate(mContext, R.layout.bar_view, this);

            Random random = new Random();

            mBar = findViewById(R.id.bar);
            mBar.setBackgroundColor(
                    Color.rgb(random.nextInt(135) + 120,
                            random.nextInt(100),
                            random.nextInt(105) + 150));
            mBar.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    popup();
                }
            });

            mText = (TextView) findViewById(R.id.text);
        }

        private void popup() {
            final LinearLayout popup = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.input_dialog_view, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setView(popup);
            builder.setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            int red = Integer.parseInt(((EditText) popup.findViewById(R.id.edit_color_red)).getText().toString());
                            int green = Integer.parseInt(((EditText) popup.findViewById(R.id.edit_color_green)).getText().toString());
                            int blue = Integer.parseInt(((EditText) popup.findViewById(R.id.edit_color_blue)).getText().toString());

                            if(0 > red || red > 255) {
                                if(0 > green || green > 255) {
                                    if(0 > blue || blue > 255) {
                                        Toast.makeText(mContext, "Enter a number between 0-255!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                            }
                            mBar.setBackgroundColor(Color.rgb(red, green, blue));

                            Toast.makeText(mContext, "Success!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(mContext, "Canceled", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        }
    }

    private class GsonObject {

        private List<LinkedList<Float>> mList;

        private GsonObject() {

        }

    }
}
