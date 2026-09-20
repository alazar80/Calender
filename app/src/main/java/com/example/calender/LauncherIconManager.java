package com.example.calender;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Keeps the launcher icon matched to the current day of month.
 *
 * Android does not let an app replace its launcher bitmap with an arbitrary
 * image at runtime. The supported approach is to predeclare launcher aliases
 * and enable exactly one of them.
 */
public final class LauncherIconManager {

    private static final String DEFAULT_ALIAS = ".CalendarDefault";
    private static final String DAY_ALIAS_PREFIX = ".CalendarDay";
    private static final String WORK_PREFIX = "calendarIconRollover-";

    private LauncherIconManager() {
        // Utility class.
    }

    /** Update today's icon now and make sure the next midnight update is queued. */
    public static void syncAndSchedule(Context context) {
        Context appContext = context.getApplicationContext();
        updateForToday(appContext);
        scheduleNextMidnight(appContext);
    }

    /** Enable today's launcher alias and disable every stale launcher alias. */
    public static void updateForToday(Context context) {
        Context appContext = context.getApplicationContext();
        PackageManager packageManager = appContext.getPackageManager();
        String packageName = appContext.getPackageName();
        int today = LocalDate.now().getDayOfMonth();

        ComponentName todayAlias = componentForDay(packageName, today);

        // Enable today's icon first so the launcher never temporarily loses the app.
        setState(
                packageManager,
                todayAlias,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        );

        setState(
                packageManager,
                new ComponentName(packageName, packageName + DEFAULT_ALIAS),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        );

        for (int day = 1; day <= 31; day++) {
            if (day == today) {
                continue;
            }

            setState(
                    packageManager,
                    componentForDay(packageName, day),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            );
        }
    }

    /** Queue one update for the next local midnight; the worker queues the following day. */
    public static void scheduleNextMidnight(Context context) {
        Context appContext = context.getApplicationContext();

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime nextMidnight = now.toLocalDate()
                .plusDays(1)
                .atStartOfDay(now.getZone());

        long delayMillis = Math.max(
                1_000L,
                Duration.between(now, nextMidnight).toMillis()
        );

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DailyIconWorker.class)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .build();

        // Date-specific name lets the running worker safely schedule tomorrow.
        String workName = WORK_PREFIX + nextMidnight.toLocalDate();

        WorkManager.getInstance(appContext)
                .enqueueUniqueWork(
                        workName,
                        ExistingWorkPolicy.REPLACE,
                        request
                );
    }

    private static ComponentName componentForDay(String packageName, int day) {
        String aliasName = String.format(
                java.util.Locale.ROOT,
                "%s%s%02d",
                packageName,
                DAY_ALIAS_PREFIX,
                day
        );
        return new ComponentName(packageName, aliasName);
    }

    private static void setState(
            PackageManager packageManager,
            ComponentName component,
            int desiredState
    ) {
        if (packageManager.getComponentEnabledSetting(component) == desiredState) {
            return;
        }

        packageManager.setComponentEnabledSetting(
                component,
                desiredState,
                PackageManager.DONT_KILL_APP
        );
    }
}
