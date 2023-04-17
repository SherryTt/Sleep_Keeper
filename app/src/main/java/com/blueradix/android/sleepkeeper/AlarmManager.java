package com.blueradix.android.sleepkeeper;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;

public class AlarmManager extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MediaPlayer alarm = MediaPlayer.create(this,R.raw.alarm);
        alarm.start();
    }
}