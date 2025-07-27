package com.example.calender;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.calender.MainActivity;

import java.time.LocalDate;

public class DailyIconWorker extends Worker {
    public DailyIconWorker(@NonNull Context ctx, @NonNull WorkerParameters params) {
        super(ctx, params);
    }

    @NonNull @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        PackageManager pm = ctx.getPackageManager();
        String pkg = ctx.getPackageName();

        // disable all 31 aliases
        for (int d = 1; d <= 31; d++) {
            String alias = String.format("%s.CalendarDay%02d", pkg, d);
            pm.setComponentEnabledSetting(
                    new ComponentName(pkg, alias),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
            );
        }

        // enable today’s alias
        int today = LocalDate.now().getDayOfMonth();
        String todayAlias = String.format("%s.CalendarDay%02d", pkg, today);
        pm.setComponentEnabledSetting(
                new ComponentName(pkg, todayAlias),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );

        // schedule next midnight run
        MainActivity.scheduleDailyIconUpdate(ctx);
        return Result.success();
    }
}
