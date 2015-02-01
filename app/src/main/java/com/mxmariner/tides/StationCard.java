package com.mxmariner.tides;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mxmariner.andxtidelib.IStationData;

public class StationCard extends CardView {

    private TextView nameTv;
    private TextView dateTv;
    private TextView predictionTv;
    private LinearLayout detailsLayout;
    private int currentIndex;

    public StationCard(Context context) {
        super(context);
        init();
    }

    public StationCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StationCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.station_card, this);
        nameTv = (TextView) findViewById(R.id.station_card_station_name);
        dateTv = (TextView) findViewById(R.id.station_card_station_datetime);
        predictionTv = (TextView) findViewById(R.id.station_card_station_prediction);
        detailsLayout = (LinearLayout) findViewById(R.id.station_card_details_container);
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public void applyStationData(final IStationData data, int index) {
        if (index != currentIndex) {
            return;
        }

        nameTv.setText(data.getName());
        dateTv.setText(data.getDataTimeStamp());
        predictionTv.setText(data.getPrediction());
        detailsLayout.removeAllViews();
        String[] plainData = data.getPlainData();
        for (int i=1; i<plainData.length; i++) {
            TextView tv = new TextView(getContext());
            tv.setText(plainData[i].trim());
            if (i % 2 != 0) {
                tv.setBackgroundColor(getResources().getColor(R.color.accent));
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setTextColor(getResources().getColor(R.color.secondary_text));
            }
            detailsLayout.addView(tv);
        }

    }

}
