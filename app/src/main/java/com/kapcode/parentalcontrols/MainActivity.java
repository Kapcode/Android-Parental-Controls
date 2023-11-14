package com.kapcode.parentalcontrols;

import androidx.appcompat.app.AppCompatActivity;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
static AlarmManager alarmManager;
static Context context;
    static PendingIntent pendingWatchDogIntent;
    static Intent watchDogIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        context=this;

    }
    public static void startService(){
        // <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
        // <service android:enabled="true" android:name="com.kapcode.parentalcontrols.ExampleService" />
        stopService();
        ExampleService.serviceIsRunning.set(true);
        Intent ServiceIntent = new Intent(context, ExampleService.class);
        context.startService(ServiceIntent);
    }
    public void startButton(View v){
        //watchDog starts the Alarm, Watching the service
        startService();
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        watchDog(alarmManager,context);
    }
    public void stopButton(View v){
        stopService();
    }


    public static void watchDog(AlarmManager alarmManager , Context context){
        watchDogIntent = new Intent(context, MyAlarmReceiver.class);
        pendingWatchDogIntent = PendingIntent.getBroadcast(context, 0, watchDogIntent, PendingIntent.FLAG_MUTABLE);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.add(Calendar.SECOND, 10);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingWatchDogIntent);
    }


    public static void stopService(){// button press
        ExampleService.serviceStoppedByUser.set(true);
        if(pendingWatchDogIntent!=null)pendingWatchDogIntent.cancel();
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