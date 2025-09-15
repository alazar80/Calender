package com.example.calender;

import android.app.DatePickerDialog;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.calender.util.DateUtils; // your existing converter utils

import java.util.Calendar;

/**
 * Converter screen:
 *  - Ethiopian → Gregorian  => opens custom Ethiopian date picker (NumberPickers)
 *  - Gregorian → Ethiopian  => opens standard Android DatePickerDialog
 */
public class ConverterFragment extends Fragment {

    private RadioGroup rgType;     // R.id.rgType
    private TextView tvResult;     // R.id.tvResult
    private Button btnPick;        // R.id.btnPick

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_converter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rgType   = view.findViewById(R.id.rgType);
        tvResult = view.findViewById(R.id.tvResult);
        btnPick  = view.findViewById(R.id.btnPick);

        btnPick.setOnClickListener(v -> {
            boolean isEtoG = rgType.getCheckedRadioButtonId() == R.id.rbEtoG;

            if (isEtoG) {
                // ETHIOPIAN → GREGORIAN: open custom Ethiopian dialog
                showEthiopianDateDialog((y, m, d) -> {
                    // m is 1..13 from NumberPicker; DO NOT +1
                    String out = DateUtils.convertEthiopianToGregorian(y, m, d);
                    tvResult.setText(out);
                });
            } else {
                // GREGORIAN → ETHIOPIAN: open normal date picker
                Calendar now = Calendar.getInstance();
                new DatePickerDialog(
                        requireContext(),
                        (picker, y, m0, d) -> {
                            // DatePicker month is 0-based
                            String out = DateUtils.convertGregorianToEthiopian(y, m0 + 1, d);
                            tvResult.setText(out);
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });
    }

    // ==== Ethiopian Picker dialog ====

    private interface OnEthDatePicked {
        void onPicked(int year, int month, int day);
    }

    private void showEthiopianDateDialog(OnEthDatePicked cb) {
        final String[] ETH_MONTHS = {
                "መስከረም", "ጥቅምት", "ህዳር", "ታኅሣሥ",
                "ጥር", "የካቲት", "መጋቢት", "ሚያዝያ",
                "ግንቦት", "ሰኔ", "ሐምሌ", "ነሐሴ", "ጳጉሜን"
        };

        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_ethiopian_date_picker, null, false);

        NumberPicker npYear  = content.findViewById(R.id.npYear);
        NumberPicker npMonth = content.findViewById(R.id.npMonth);
        NumberPicker npDay   = content.findViewById(R.id.npDay);

        // Start from "today" converted to Ethiopian
        Calendar now = Calendar.getInstance();
        String[] etParts = DateUtils.convertGregorianToEthiopian(
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DAY_OF_MONTH)
        ).split("-"); // expects "YYYY-MM-DD"

        int eYear  = safeParse(etParts, 0, 2016); // sane defaults
        int eMonth = safeParse(etParts, 1, 1);
        int eDay   = safeParse(etParts, 2, 1);

        // Year
        npYear.setMinValue(1900);
        npYear.setMaxValue(2100);
        npYear.setValue(eYear);
        npYear.setWrapSelectorWheel(false);

        // Month 1..13 with Amharic names
        npMonth.setMinValue(1);
        npMonth.setMaxValue(13);
        npMonth.setDisplayedValues(null); // avoid IllegalArgumentException when re-setting
        npMonth.setDisplayedValues(ETH_MONTHS);
        npMonth.setValue(eMonth);
        npMonth.setWrapSelectorWheel(false);

        // Day depends on month & leap year
        npDay.setMinValue(1);
        npDay.setMaxValue(daysInEthiopianMonth(eYear, eMonth));
        npDay.setValue(Math.min(eDay, npDay.getMaxValue()));
        npDay.setWrapSelectorWheel(false);

        NumberPicker.OnValueChangeListener recalc = (picker, oldVal, newVal) -> {
            int y = npYear.getValue();
            int m = npMonth.getValue();
            int max = daysInEthiopianMonth(y, m);
            int current = npDay.getValue();
            npDay.setMaxValue(max);
            if (current > max) npDay.setValue(max);
        };

        npYear.setOnValueChangedListener(recalc);
        npMonth.setOnValueChangedListener(recalc);

        new AlertDialog.Builder(requireContext())
                .setTitle("ቀን መርጫ")
                .setView(content)
                .setNegativeButton("ይቅር", null)
                .setPositiveButton("እሺ", (d, which) ->
                        cb.onPicked(npYear.getValue(), npMonth.getValue(), npDay.getValue()))
                .show();
        // If you use Material Components, swap to:
        // new MaterialAlertDialogBuilder(requireContext()) ... .show();
    }

    /** Ethiopian month lengths: 1–12 = 30 days; 13 = 5 (or 6 in leap years). */
    private int daysInEthiopianMonth(int year, int month) {
        if (month <= 12) return 30;
        return isEthiopianLeapYear(year) ? 6 : 5;
    }

    /** Ethiopian leap year rule: years ≡ 3 (mod 4). */
    private boolean isEthiopianLeapYear(int year) {
        return year % 4 == 3;
    }

    private int safeParse(String[] parts, int index, int fallback) {
        try {
            return Integer.parseInt(parts[index]);
        } catch (Exception ignore) {
            return fallback;
        }
    }
}