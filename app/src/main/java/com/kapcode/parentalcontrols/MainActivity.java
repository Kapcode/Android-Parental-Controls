package com.kapcode.parentalcontrols;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    public void startService(View v){
        // <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
        // <service android:enabled="true" android:name="com.kapcode.parentalcontrols.ExampleService" />
        stopService(v);
        ExampleService.serviceIsRunning.set(true);
        Intent i = new Intent(this, ExampleService.class);
        this.startService(i);
    }

    public void stopService(View v){
        ExampleService.serviceIsRunning.set(false);
        if(ExampleService.serviceThread !=null){
            ExampleService.serviceThread.interrupt();
            try {
                ExampleService.serviceThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

}