package com.example.calender;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {
    private SwitchCompat swOrth, swIslamic, swEpl;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        return i.inflate(R.layout.fragment_settings, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, Bundle s) {
        super.onViewCreated(v, s);
        prefs = requireContext()
                .getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        swOrth = v.findViewById(R.id.swOrthodox);
        swIslamic = v.findViewById(R.id.swIslamic);
        swEpl = v.findViewById(R.id.swEPL);

        // load saved
        swOrth.setChecked(prefs.getBoolean("orthodox", false));
        swIslamic.setChecked(prefs.getBoolean("islamic", false));
        swEpl.setChecked(prefs.getBoolean("epl", false));

        // listeners
        swOrth.setOnCheckedChangeListener(this::onToggle);
        swIslamic.setOnCheckedChangeListener(this::onToggle);
        swEpl.setOnCheckedChangeListener(this::onToggle);
    }

    private void onToggle(CompoundButton btn, boolean on) {
        String key = btn.getId()==R.id.swOrthodox ? "orthodox"
                : btn.getId()==R.id.swIslamic ? "islamic"
                : "epl";
        prefs.edit().putBoolean(key, on).apply();
    }
}

