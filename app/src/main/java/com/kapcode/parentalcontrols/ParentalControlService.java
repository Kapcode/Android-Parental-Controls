package com.kapcode.parentalcontrols;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParentalControlService extends Service {
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
                        printForegroundTask();
                        Thread.sleep(10000);

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

    private void printForegroundTask() {//Settings -> Security -> (Scroll down to last) Apps with usage access -> Give the permissions to our app
        String currentApp = "NULL";
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }
        System.out.println("Current App in foreground is: " + currentApp);
    }

    public void printRunningProcesses(){//returns only this app
        ActivityManager manager =
                (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo process : processes)
        {
            System.out.println(process.processName);
        }
    }





}
