package com.blueradix.android.sleepkeeper;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.room.Room;


import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYGraphWidget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
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
import java.util.List;
import java.util.Map;


public class HRActivity extends AppCompatActivity implements PlotterListener  {

    private PolarBleApi api;
    private Context classContext = this;
    private String device_id;
    private TextView textViewHrNum;
    private TextView textViewRrNum;
    private TextView textViewNightmareNum;
    private TextView textViewSleepRate;
    private XYPlot plot;
    private TimePlotter plotter;
    private static final String TAG = "HRActivity";

    // First, initialize Firebase



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hr_graph);

        textViewHrNum = findViewById(R.id.textViewHrNum);
        textViewRrNum = findViewById(R.id.textViewRrNum);
        textViewNightmareNum = findViewById(R.id.textViewNightmareNum);
        textViewSleepRate = findViewById(R.id.textViewRate);
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

                if (!data.getRrsMs().isEmpty()) {
                    String rrText = "(" + data.getRrsMs().stream().map(Object::toString).reduce((s, s2) -> s + "ms, " + s2).orElse("") + "ms)";
                    textViewRrNum.setText(rrText);
                }
                textViewHrNum.setText(String.valueOf(data.getHr()));
                plotter.addValues(data);
                addData(data);
/*
                int average = calcAve();
                int min = calcMin();
                int max = calcMax();

                // get the Room database instance
                HRdataRoomDatabase db = HRdataRoomDatabase.getDatabase(getApplicationContext());
                HRdataDao hrdatadao = db.hrdataDao();
                HRdata hrdata = new HRdata(data.getHr()/*,data.getRrsMs()*//*,System.currentTimeMillis(),average,max,min);
                hrdatadao.insert(hrdata);*/


            }
/*
            public int calcAve(){return calcAve;}
            public int calcMin(){return calcMin;}
            public int calcMax(){return calcMax;}*/

            public void addData(PolarHrData data){

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                Map<String,Object> Hrdata = new HashMap<>();
                Hrdata.put("heart_rate",data.getHr());
                Hrdata.put("rr_ms",data.getRrsMs());

                db.collection("Hr_data").add(Hrdata)
                        .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()))
                        .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));

            }

            /* userId
            public void addData1(PolarHrData data){

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DocumentReference docRef = db.collection("users").document(userId)
                        .collection("HRdata").document();
                Map<String,Object> Hrdata = new HashMap<>();
                Hrdata.put("heart_rate",data.getHr());
                Hrdata.put("rr_ms",data.getRrsMs());

                docRef.set(Hrdata)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + docRef.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });
            }*/

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

