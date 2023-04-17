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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.PolarBleApiDefaultImpl;
import com.polar.sdk.api.PolarBleApiCallback;
import com.polar.sdk.api.model.PolarDeviceInfo;
import com.polar.sdk.api.errors.PolarInvalidArgument;
import com.polar.sdk.api.model.PolarHrData;
import java.util.HashMap;


import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ExecutionException;


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
    private int minHr = 0;
    private int findMin;
    private int maxHr;
    private  int averageHr;


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

                addData(data,minHr,maxHr,averageHr);

            }


            //Insert HR data into DB
            public void addData(PolarHrData data,int minHr,int maxHr,int averageHr){
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
                hrData.put("average_hr",minHr);
                hrData.put("max_hr",minHr);
                hrData.put("min_hr",minHr);

                docRef.set(hrData)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "HR data added: " + hrData))
                        .addOnFailureListener(e -> Log.e(TAG, "Error adding HR data", e));
            }


            //Find minimum HR value
            public void getMinimumHeartRate(){

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                // Access the "heart_rate" collection
                CollectionReference heartRateRef = db.collection("HRdata");

                // Query the collection to get the minimum heart rate value
                Query query = heartRateRef.orderBy("heart_rate", Query.Direction.ASCENDING).limit(1);

                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Check if the result set is not empty
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Get the first document in the result set
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            // Get the heart rate value
                            int minimumHr = documentSnapshot.getLong("heart_rate").intValue();
                            passMinHr(minimumHr);
                        } else {
                           int minimumHr = 0;
                            passMinHr(minimumHr);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Log error message
                        Log.d(TAG, "Error getting minimum heart rate value", e);
                    }
                });
            }

            public int passMinHr(int minHr){
                return minHr;
            }

            //Find maxmum HR value
            public void getMaxiHeartRate(){

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                // Access the "heart_rate" collection
                CollectionReference heartRateRef = db.collection("HRdata");

                // Query the collection to get the minimum heart rate value
                Query query = heartRateRef.orderBy("heart_rate", Query.Direction.DESCENDING).limit(1);

                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Check if the result set is not empty
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Get the first document in the result set
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            // Get the heart rate value
                            int maxmumHr = documentSnapshot.getLong("heart_rate").intValue();
                            passMinHr(maxmumHr);
                        } else {
                            int maxmumHr = 0;
                            passMaxHr(maxmumHr);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Log error message
                        Log.d(TAG, "Error getting maxmum heart rate value", e);
                    }
                });
            }

            public int passMaxHr(int maxHr){
                return maxHr;
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

