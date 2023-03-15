package com.blueradix.android.sleepkeeper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private String TAG = "Polar_MainActivity";
    private String sharedPrefsKey = "polar_device_id";
    private String DEVICE_ID;
    private ActivityResultLauncher bluetoothOnActivityResultLauncher;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        checkBT();
    }

    public void onClickConnectHr(View view) {
        checkBT();
        DEVICE_ID = sharedPreferences.getString(sharedPrefsKey,"");
        Log.d(TAG,DEVICE_ID);
        if(DEVICE_ID.equals("")){
            showDialog(view);
        } else {
            Toast.makeText(this,getString(R.string.connecting) + " " + DEVICE_ID,Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, HRGraph.class);
            intent.putExtra("id", DEVICE_ID);
            startActivity(intent);
        }
    }


    public void onClickChangeID(View view) {
        showDialog(view);
    }

    public void showDialog(View view){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enter your Polar device's ID");

        View viewInflated = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_select_ble,(ViewGroup) view.getRootView() , false);

        final EditText input = viewInflated.findViewById(R.id.input);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        dialog.setView(viewInflated);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DEVICE_ID = input.getText().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(sharedPrefsKey, DEVICE_ID);
                editor.apply();
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    public void checkBT(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
            this.bluetoothOnActivityResultLauncher.launch(enableBtIntent);
        }

        //requestPermissions() method needs to be called when the build SDK version is 23 or above
        if(Build.VERSION.SDK_INT >= 23) {
            this.requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    public void showToast(String message){
        Toast toast = Toast.makeText(this.getApplicationContext(),message,Toast.LENGTH_SHORT);
        toast.show();
    }
}
