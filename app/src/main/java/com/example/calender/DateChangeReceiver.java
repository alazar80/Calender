package com.example.calender;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DateChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        // Re-run your alias toggling logic on boot and on date changes
        MainActivity.updateLauncherIconForToday(ctx);
    }
}
