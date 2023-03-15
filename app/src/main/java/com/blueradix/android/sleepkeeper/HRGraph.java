package com.blueradix.android.sleepkeeper;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.androidplot.xy.XYPlot;
import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.PolarBleApiDefaultImpl;
import com.polar.sdk.api.PolarBleApiCallback;
import com.polar.sdk.api.model.PolarDeviceInfo;
import com.polar.sdk.api.model.PolarHrData;

import java.util.Date;
import java.util.List;


public class HRGraph extends AppCompatActivity {

    private PolarBleApi api;
    private Context classContext = this;
    private String device_id;
  //  private HrAndRrPlotter plotter;
    private TextView textViewAve;
    private TextView textViewMax;
    private TextView textViewMin;
    private TextView textViewId;
    private XYPlot plot;
    private String TAG = "HRGraph";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr_graph);
        textViewId = findViewById(R.id.textViewID);
        textViewAve = findViewById(R.id.textViewAve);
        textViewMax = findViewById(R.id.textViewMax);
        textViewMin = findViewById(R.id.textViewMin);
        device_id = getIntent().getStringExtra("id");


        api = PolarBleApiDefaultImpl.defaultImplementation(this,
                PolarBleApi.FEATURE_BATTERY_INFO |
                        PolarBleApi.FEATURE_DEVICE_INFO |
                        PolarBleApi.FEATURE_HR);



        api.setApiCallback(new PolarBleApiCallback() {
            @Override
            public void blePowerStateChanged(boolean b) {
                Log.d(TAG, "--------------------- BluetoothStateChanged " + b);
            }

            @Override
            public void deviceConnected(PolarDeviceInfo s) {
                Log.d(TAG, "Device connected " + s.getDeviceId());
                Toast.makeText(classContext, R.string.connected,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deviceConnecting(PolarDeviceInfo polarDeviceInfo) {
            }

            @Override
            public void deviceDisconnected(PolarDeviceInfo s) {
                Log.d(TAG, "-----------------------Device disconnected " + s);
                Toast.makeText(classContext, R.string.disconnected,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }
}
