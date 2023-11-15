package com.kapcode.parentalcontrols;

import androidx.appcompat.app.AppCompatActivity;


import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
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
    public void grantAccessButton(View v){
        String title = "";
        String message = "";
        String pos = "";
        String neg = "";
    if (isAccessUsageAccessGranted()) {
         title = "Usage Access Already Granted";
         message = "Do you want do revoke Usage Access Permission by going to setting now?\n" +
                 "NOTE: This will disable parts of the app.";
         pos = "Yes";
         neg = "No";
        }else{
        title = "Grant Usage Access?";
        message = "You will be taken to setting to grant usage access to this app, is that okay?\n" +
                "Reason for usage access: to get current running application. (NO DATA IS COLLECTED)";
        pos = "YES";
        neg = "NO";
        }
            new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(pos, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            startActivity(intent);
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(neg, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();



    }
    private boolean isAccessUsageAccessGranted() {//<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" tools:ignore="ProtectedPermissions"/>
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        context=this;
        getInstalledApplications(this);

        RadioButton whiteListB = findViewById(R.id.whitelistRB);
        RadioButton blacklistB = findViewById(R.id.blacklistRB);

        whiteListB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //turning on whitelist
                if(blacklistB.isChecked()){
                    whiteListB.setChecked(true);
                    blacklistB.setChecked(false);
                }
            }
        });
        blacklistB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //turning on blacklist
                if(whiteListB.isChecked()){
                    whiteListB.setChecked(false);
                    blacklistB.setChecked(true);
                }
            }
        });

    }
    public static void startService(){
        // <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
        // <service android:enabled="true" android:name="com.kapcode.parentalcontrols.ParentalControlService" />
        stopService();
        ParentalControlService.serviceIsRunning.set(true);
        Intent ServiceIntent = new Intent(context, ParentalControlService.class);
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
        ParentalControlService.serviceStoppedByUser.set(true);
        if(pendingWatchDogIntent!=null)pendingWatchDogIntent.cancel();
        ParentalControlService.serviceIsRunning.set(false);
        if(ParentalControlService.serviceThread !=null){
            ParentalControlService.serviceThread.interrupt();
            try {
                ParentalControlService.serviceThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
    public void getInstalledApplications(Context context){
        ArrayList<CheckBox> checkBoxes = new ArrayList<>();
    PackageManager packageManager = getPackageManager();
    List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.MATCH_ALL);
       LinearLayout scroll_layout = findViewById(R.id.scroll_layout);
    for(ApplicationInfo applicationInfo: installedApplications){
        System.out.println(applicationInfo.packageName);

        Drawable drawable = null;
        try {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            //layout.setLayoutParams(new ViewGroup.LayoutParams(100,100));
            ImageView iv = new ImageView(context);
            drawable = context.getPackageManager().getApplicationIcon(applicationInfo.packageName);

            iv.setImageDrawable(drawable);
            TextView textView = new TextView(context);
            textView.setText(applicationInfo.packageName);
            CheckBox checkBox = new CheckBox(context);
            layout.addView(checkBox);
            layout.addView(iv);
            layout.addView(textView);

            scroll_layout.addView(layout);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) layout.getLayoutParams();
            lp.height=100;
            lp.setMarginStart(60);
            textView.setTextSize(18);
            layout.setLayoutParams(lp);
            checkBoxes.add(checkBox);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBox.setChecked(!checkBox.isChecked());
                }
            });
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBox.setChecked(!checkBox.isChecked());
                }
            });
        } catch (PackageManager.NameNotFoundException e) {

        }





    }


    }

    private void killAppBypackage(String packageToKill){

        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = getPackageManager();
        //get a list of installed apps.
        packages = pm.getInstalledApplications(0);


        ActivityManager mActivityManager = (ActivityManager) MainActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
        String myPackage = getApplicationContext().getPackageName();

        for (ApplicationInfo packageInfo : packages) {

            if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM)==1) {
                continue;
            }
            if(packageInfo.packageName.equals(myPackage)) {
                continue;
            }
            if(packageInfo.packageName.equals(packageToKill)) {
                mActivityManager.killBackgroundProcesses(packageInfo.packageName);
            }

        }

    }



}