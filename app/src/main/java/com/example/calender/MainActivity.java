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
import java.time.LocalDateTime;
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

        // 4) Launcher‑icon logic unchanged
//        updateLauncherIconForToday();
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
                PackageManager pm = ctx.getPackageManager();
                String pkg = ctx.getPackageName();  // unchanged

        for (int d = 1; d <= 31; d++) {
            String alias = String.format("%s.CalendarDay%02d", pkg, d);
            pm.setComponentEnabledSetting(
                    new ComponentName(pkg, alias),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
            );
        }
        int today = LocalDate.now().getDayOfMonth();
        String todayAlias = String.format("%s.CalendarDay%02d", pkg, today);
        pm.setComponentEnabledSetting(
                new ComponentName(pkg, todayAlias),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );
    }

    /** Your scheduler to run at next midnight */
    public static void scheduleDailyIconUpdate(Context ctx) {
        LocalDateTime now     = LocalDateTime.now();
        LocalDateTime nextMid = now.toLocalDate().plusDays(1).atStartOfDay();
        long delayMinutes     = Duration.between(now, nextMid).toMinutes();

        OneTimeWorkRequest w = new OneTimeWorkRequest.Builder(DailyIconWorker.class)
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(ctx)
                .enqueueUniqueWork(
                        "dailyIconUpdate",
                        ExistingWorkPolicy.REPLACE,
                        w
                );
    }

    // stubs for non‑fragment menu items:
    private void showHelp()   { /* TODO */ }
    private void shareApp()   { /* TODO */ }
    private void rateApp()    { /* TODO */ }
    private void showAbout()  { /* TODO */ }
}
