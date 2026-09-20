package com.example.calender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DateChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            MainActivity.updateLauncherIconForToday(ctx);
            MainActivity.scheduleDailyIconUpdate(ctx);
        }
    }
}
