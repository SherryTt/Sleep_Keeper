package com.blueradix.android.sleepkeeper;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.androidplot.xy.XYPlot;
import com.polar.sdk.api.PolarBleApi;

public class HRGraph extends AppCompatActivity {
    private PolarBleApi api;
  //  private HrAndRrPlotter plotter;
    private TextView textViewAve;
    private TextView textViewMax;
    private TextView textViewMin;
    private TextView textViewId;
    private XYPlot plot;
    private static final String TAG = "HRGraph";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr_graph);
        String DEVICE_ID = this.getIntent().getStringExtra("id");
        textViewId = findViewById(R.id.textViewID);
        textViewAve = findViewById(R.id.textViewAve);
        textViewMax = findViewById(R.id.textViewMax);
        textViewMin = findViewById(R.id.textViewMin);

    }


}


