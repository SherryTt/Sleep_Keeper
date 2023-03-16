package com.blueradix.android.sleepkeeper;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYGraphWidget;
import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.PolarBleApiDefaultImpl;
import com.polar.sdk.api.PolarBleApiCallback;
import com.polar.sdk.api.model.PolarDeviceInfo;
import com.polar.sdk.api.errors.PolarInvalidArgument;
import com.polar.sdk.api.model.PolarHrData;


import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class HRGraph extends AppCompatActivity implements PlotterListener  {

    private PolarBleApi api;
    private Context classContext = this;
    private String device_id;
  //  private HrAndRrPlotter plotter;
    private TextView textViewAve;
    private TextView textViewMax;
    private TextView textViewMin;
    private TextView textViewId;
    private XYPlot plot;
    private TimePlotter plotter;
    private static final String TAG = "HRGraph";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr_graph);
        textViewId = findViewById(R.id.textViewID);
        textViewAve = findViewById(R.id.textViewAve);
        textViewMax = findViewById(R.id.textViewMax);
        textViewMin = findViewById(R.id.textViewMin);
        device_id = getIntent().getStringExtra("id");
        plot = findViewById(R.id.hr_view_plot);

        api = PolarBleApiDefaultImpl.defaultImplementation(this,
                PolarBleApi.FEATURE_BATTERY_INFO |
                        PolarBleApi.FEATURE_DEVICE_INFO |
                        PolarBleApi.FEATURE_HR);


        api.setApiLogger(str -> Log.d("SDK", str));
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
/*
            @Override
            public void streamingFeaturesReady(@NonNull final String identifier,
                                               @NonNull final Set<PolarBleApi.DeviceStreamingFeature> features) {

                for (PolarBleApi.DeviceStreamingFeature feature : features) {
                    Log.d(TAG, "Streaming feature is ready: " + feature);
                    switch (feature) {
                        case ECG:
                        case ACC:
                        case MAGNETOMETER:
                        case GYRO:
                        case PPI:
                        case PPG:
                            break;
                    }
                }
            }*/
            @Override
            public void hrFeatureReady(@NonNull String s) {
                Log.d(TAG, "HR Feature ready " + s);
            }
/*
            @Override
            public void disInformationReceived(@NonNull String s, @NonNull UUID u, @NonNull String s1) {
                if (u.equals(UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb"))) {
                    String msg = "Firmware: " + s1.trim();
                    Log.d(TAG, "Firmware: " + s + " " + s1.trim());
                    textViewFW.append(msg + "\n");
                }
            }

            @Override
            public void batteryLevelReceived(@NonNull String s, int i) {
                String msg = "ID: " + s + "\nBattery level: " + i;
                Log.d(TAG, "Battery level " + s + " " + i);
//                Toast.makeText(classContext, msg, Toast.LENGTH_LONG).show();
                textViewFW.append(msg + "\n");
            }*/
            @Override
            public void hrNotificationReceived(@NonNull String s,
                                               @NonNull PolarHrData polarHrData) {
                Log.d(TAG, "HR " + polarHrData.getHr());
                List<Integer> rrsMs = polarHrData.getRrsMs();
                StringBuilder msg = new StringBuilder(polarHrData.getHr() + "\n");
                for (int i : rrsMs) {
                    msg.append(i).append(",");
                }
                if (msg.toString().endsWith(",")) {
                    msg = new StringBuilder(msg.substring(0, msg.length() - 1));
                }
                textViewAve.setText(msg.toString());
                plotter.addValues(polarHrData);
            }

            @Override
            public void polarFtpFeatureReady(@NonNull String s) {
                Log.d(TAG, "Polar FTP ready " + s);
            }

        });
        try {
            api.connectToDevice(device_id);
        } catch (PolarInvalidArgument a) {
            a.printStackTrace();
        }

        plotter = new TimePlotter();
        plotter.setListener(this);

        plot.addSeries(plotter.getHrSeries(), plotter.getHrFormatter());
        plot.addSeries(plotter.getRrSeries(), plotter.getRrFormatter());
        plot.setRangeBoundaries(50, 100, BoundaryMode.AUTO);
        plot.setDomainBoundaries(0, 360000, BoundaryMode.AUTO);
        // Left labels will increment by 10
        plot.setRangeStep(StepMode.INCREMENT_BY_VAL, 10);
        plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 60000);
        // Make left labels be an integer (no decimal places)
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).
                setFormat(new DecimalFormat("#"));
        // These don't seem to have an effect
        plot.setLinesPerRangeLabel(2);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        api.shutDown();
    }

    public void update() {
        runOnUiThread(() -> plot.redraw());
    }
}

