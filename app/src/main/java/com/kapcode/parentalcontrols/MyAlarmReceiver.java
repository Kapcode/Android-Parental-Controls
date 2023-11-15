package com.kapcode.parentalcontrols;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Alarm went off", Toast.LENGTH_SHORT).show();
        //user didn't stop it, and it is not running, android must have stopped it
        if(!ParentalControlService.serviceStoppedByUser.get() && !ParentalControlService.serviceIsRunning.get()){
            //Restart It Here
            System.out.println("Restart Code Here TODO");
            MainActivity.startService();
        }else{
            System.out.println("All Good");
        }


        //start watchdog again.
        MainActivity.watchDog(MainActivity.alarmManager,MainActivity.context);

    }
}