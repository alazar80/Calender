package com.example.calender;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class DailyIconWorker extends Worker {

    public DailyIconWorker(@NonNull Context ctx, @NonNull WorkerParameters params) {
        super(ctx, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            LauncherIconManager.syncAndSchedule(getApplicationContext());
            return Result.success();
        } catch (RuntimeException error) {
            return Result.retry();
        }
    }
}
