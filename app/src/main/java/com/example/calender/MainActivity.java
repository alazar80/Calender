package com.example.calender;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // UI
    private TextView tvInfo;
    private Button btnShowDate;
    private TextView tvDate;
    private DrawerLayout drawer;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupToolbarAndDrawer();
        setupNavigation();

        // 2) On‑click shows today’s date
//        btnShowDate.setOnClickListener(v -> displayTodayDate());
        // 3) Optionally show it immediately
//        displayTodayDate();

        // Keep the launcher icon synchronized immediately, then schedule the next rollover.
        updateLauncherIconForToday(this);
        scheduleDailyIconUpdate(this);
    }

    private void bindViews() {
//        tvInfo      = findViewById(R.id.tvInfo);
//        btnShowDate = findViewById(R.id.btnShowDate);
//        tvDate      = findViewById(R.id.tvDate);
        drawer      = findViewById(R.id.drawer_layout);
        navView     = findViewById(R.id.nav_view);
    }

    private void setupToolbarAndDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.nav_open, R.string.nav_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupNavigation() {
        navView.setNavigationItemSelectedListener(this);
        // default to Calendar (“Agenda”)
        if (getSupportFragmentManager().findFragmentById(R.id.content_frame) == null) {
            navView.setCheckedItem(R.id.nav_agenda);
            loadFragment(new CalendarFragment());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment frag = null;
        int id = item.getItemId();

        if (id == R.id.nav_agenda) {
            frag = new CalendarFragment();
        } else if (id == R.id.nav_converter) {
            frag = new ConverterFragment();
        } else if (id == R.id.nav_settings) {
            frag = new SettingsFragment();
        } else if (id == R.id.nav_help) {
            showHelp();
        } else if (id == R.id.nav_share) {
            shareApp();
        } else if (id == R.id.nav_rate) {
            rateApp();
        } else if (id == R.id.nav_about) {
            showAbout();
        }

        if (frag != null) {
            loadFragment(frag);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /** Puts “Wednesday, Jul 17” (for example) into tvDate */
//    private void displayTodayDate() {
//        LocalDate today = LocalDate.now();
//        String formatted = today.getDayOfWeek()
//                .getDisplayName(TextStyle.FULL, Locale.getDefault())
//                + ", "
//                + today.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()));
//        tvDate.setText(formatted);
//    }

    /** Your existing logic to flip the alias on launch */
    /** Flip the alias on launch (static so receivers can call it) */
            public static void updateLauncherIconForToday(Context ctx) {
        Context appCtx = ctx.getApplicationContext();
        PackageManager pm = appCtx.getPackageManager();
        String pkg = appCtx.getPackageName();

        int today = LocalDate.now().getDayOfMonth();
        ComponentName todayComponent = new ComponentName(
                pkg,
                String.format("%s.CalendarDay%02d", pkg, today)
        );

        // Enable the new icon first so there is never a moment with no launcher entry.
        setComponentState(
                pm,
                todayComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        );

        // The generic icon is only a first-install fallback until the app runs once.
        setComponentState(
                pm,
                new ComponentName(pkg, pkg + ".CalendarDefault"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        );

        // Disable every other date icon.
        for (int d = 1; d <= 31; d++) {
            if (d == today) {
                continue;
            }
            ComponentName component = new ComponentName(
                    pkg,
                    String.format("%s.CalendarDay%02d", pkg, d)
            );
            setComponentState(
                    pm,
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            );
        }
    }

    private static void setComponentState(
            PackageManager pm,
            ComponentName component,
            int desiredState
    ) {
        if (pm.getComponentEnabledSetting(component) != desiredState) {
            pm.setComponentEnabledSetting(
                    component,
                    desiredState,
                    PackageManager.DONT_KILL_APP
            );
        }
    }

    /** Schedule one resilient update for the next local midnight. */
    public static void scheduleDailyIconUpdate(Context ctx) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime nextMidnight = now.toLocalDate()
                .plusDays(1)
                .atStartOfDay(now.getZone());

        long delayMillis = Math.max(
                1_000L,
                Duration.between(now, nextMidnight).toMillis()
        );

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(DailyIconWorker.class)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .build();

        // Use the target date in the unique name. The worker can safely schedule
        // tomorrow's job without replacing/cancelling the job that is currently running.
        String workName = "dailyIconUpdate-" + nextMidnight.toLocalDate();

        WorkManager.getInstance(ctx.getApplicationContext())
                .enqueueUniqueWork(
                        workName,
                        ExistingWorkPolicy.REPLACE,
                        work
                );
    }

    // stubs for non‑fragment menu items:
    private void showHelp()   { /* TODO */ }
    private void shareApp()   { /* TODO */ }
    private void rateApp()    { /* TODO */ }
    private void showAbout()  { /* TODO */ }
}
