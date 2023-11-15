package com.kapcode.parentalcontrols;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.atomic.AtomicBoolean;

public class ExampleService extends Service {
    static Service s;
    static volatile AtomicBoolean serviceIsRunning = new AtomicBoolean(false);
    static volatile AtomicBoolean serviceStoppedByUser = new AtomicBoolean(false);
    static volatile Thread serviceThread;
    @Override
    public void onStart(Intent intent, int startId) {
        System.out.println("Started");
        s = this;
        super.onStart(intent, startId);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("BIND");
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        System.out.println("Start");
        serviceStoppedByUser.set(false);
        serviceIsRunning.set(true);
        System.out.println("Service Create");
        // Create the Foreground Service
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_SERVICE).setContentText("Text")
                .build();
        startForeground(ID_SERVICE, notification);
        super.onCreate();
        serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int loop = 25;
                while(serviceIsRunning.get() && loop >0){ //loop counting down to 0 is simulating android killing off service, or end of work,
                    // you can use this to test watch dog, or to simulate end of work
                    System.out.println(loop);
                    loop--;
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {

                    }

                }

                serviceIsRunning.set(false);
                s.stopService(intent);
            }

        });
        serviceThread.start();


        return START_STICKY;
    }
    // Constants
    private static final int ID_SERVICE = 10129;
    @Override
    public void onCreate() {
        //super.onCreate();
        // do stuff like register for BroadcastReceiver, etc.

    }
    //requires API Level O ... 26 ... This is projects min sdk
    private String createNotificationChannel(NotificationManager notificationManager){
        String channelId = "my_service_channelid";
        String channelName = "My Foreground Service";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

}
