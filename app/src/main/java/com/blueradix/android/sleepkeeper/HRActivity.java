package com.blueradix.android.sleepkeeper;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYGraphWidget;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.auth.FirebaseAuth;

import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.PolarBleApiDefaultImpl;
import com.polar.sdk.api.PolarBleApiCallback;
import com.polar.sdk.api.model.PolarDeviceInfo;
import com.polar.sdk.api.errors.PolarInvalidArgument;
import com.polar.sdk.api.model.PolarHrData;

import java.util.HashMap;

import java.text.DecimalFormat;
import java.util.Map;


public class HRActivity extends AppCompatActivity implements PlotterListener  {

    private PolarBleApi api;
    private Context classContext = this;
    private String device_id;
    private Context context;
    private TextView textViewHrNum;
    private TextView textViewRrNum;
    private TextView textViewAveNum;
    private TextView textViewMaxNum;
    private TextView textViewMinNum;
    private MediaPlayer mediaPlayer;
    private Button stopAlarmBtn;
    private XYPlot plot;
    private TimePlotter plotter;
    private static final String TAG = "HRActivity";
    private int minHR = Integer.MAX_VALUE;
    private int maxHR = Integer.MAX_VALUE;
    private int avgHR;
    int sumHR = 0;
    int countHR = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hr_graph);
        textViewHrNum = findViewById(R.id.textViewHrNum);
        textViewMinNum = findViewById(R.id.textViewMinNum);
        textViewRrNum = findViewById(R.id.textViewRrNum);
        textViewAveNum = findViewById(R.id.textViewAveNum);
        textViewMaxNum = findViewById(R.id.textViewMaxNum);
        stopAlarmBtn = findViewById(R.id.stopAlarmBtn);
        stopAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();;
            }
        });

        plot = findViewById(R.id.hr_view_plot);
        device_id = getIntent().getStringExtra("id");
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
            public void deviceConnecting(PolarDeviceInfo s) {
                Log.d(TAG, "Device connecting " + s.getDeviceId());
            }


            @Override
            public void deviceConnected(PolarDeviceInfo s) {
                Log.d(TAG, "Device connected " + s.getDeviceId());
                Toast.makeText(classContext, R.string.connected,
                        Toast.LENGTH_SHORT).show();
            }


            @Override
            public void deviceDisconnected(PolarDeviceInfo s) {
                Log.d(TAG, "-----------------------Device disconnected " + s.getDeviceId());
                Toast.makeText(classContext, R.string.disconnected,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void hrFeatureReady(@NonNull String s) {

                Log.d(TAG, "HR Feature ready " + s);
            }

            //Receive and Display Hr/Rr data
            @Override
            public void hrNotificationReceived(String identifier,PolarHrData data) {
                Log.d(TAG, "HR " + data.getHr() + " RR " + data.getRrsMs());
                int heartRate = data.getHr();

                if (!data.getRrsMs().isEmpty()) {
                    String rrText = "(" + data.getRrsMs().stream().map(Object::toString).reduce((s, s2) -> s + "ms, " + s2).orElse("") + "ms)";
                    textViewRrNum.setText(rrText);
                }

                //Find mim and max value
                if(heartRate < minHR){
                    minHR = heartRate;
                }


                //Find the average and max by controlling count
                sumHR += heartRate;
                countHR++;
                if(countHR == 1){
                    avgHR = 0;
                    maxHR = 0;
                }
                else if(heartRate > maxHR){
                    maxHR = heartRate;
                }
                else {
                    avgHR = (countHR == 0) ? 0 : sumHR / countHR;
                }

                //Make beep sound if hr is different more than 5 comparing to avgHR
                int hrDiff = Math.abs(heartRate - avgHR);
                if ((hrDiff > 5)&&(avgHR != 0)) {
                    makeBeep();
                }


                textViewHrNum.setText(String.valueOf(data.getHr()));
                textViewMaxNum.setText(String.valueOf(maxHR));
                textViewMinNum.setText(String.valueOf(minHR));
                textViewAveNum.setText(String.valueOf(avgHR));
                plotter.addValues(data);

                addData(data,minHR,maxHR,avgHR);

            }


            //Insert HR data into DB
            public void addData(PolarHrData data,int minHr,int maxHr,int aveHr){
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                long timestamp = System.currentTimeMillis();
                String docId = Long.toString(timestamp);
                DocumentReference docRef = db.collection("users").document(userId)
                        .collection("HRdata").document(docId);
                Map<String, Object> hrData = new HashMap<>();
                hrData.put("heart_rate", data.getHr());
                hrData.put("rr_ms", data.getRrsMs());
                hrData.put("timestamp", timestamp);
                hrData.put("min_hr",minHr);
                hrData.put("max_hr",maxHr);
                hrData.put("average_hr",aveHr);

                docRef.set(hrData)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "HR data added: " + hrData))
                        .addOnFailureListener(e -> Log.e(TAG, "Error adding HR data", e));
            }



            public void makeBeep() {
                try {
                    if (mediaPlayer == null) {
                        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
                        mediaPlayer.setLooping(false);
                    }
                    mediaPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        @Override
            public void polarFtpFeatureReady(@NonNull String s) {
                Log.d(TAG, "Polar FTP ready " + s);
            }
        });
        try {
            api.connectToDevice(device_id);
            api.setAutomaticReconnection(true);
        } catch (PolarInvalidArgument a) {
            a.printStackTrace();
        }

        plotter = new TimePlotter();
        plotter.setListener(this);

        plot.addSeries(plotter.getHrSeries(), plotter.getHrFormatter());
        plot.addSeries(plotter.getRrSeries(), plotter.getRrFormatter());
        plot.setRangeBoundaries(50, 200, BoundaryMode.FIXED);
        plot.setDomainBoundaries(0, 360000, BoundaryMode.AUTO);
        // left level increment by 10
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

