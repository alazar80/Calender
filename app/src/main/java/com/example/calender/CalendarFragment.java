package com.example.calender;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import com.example.calender.util.DateUtils;  // your existing converter util

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private MaterialCalendarView calendarView;
    private TextView tvEtDayName, tvEtDayNumber, tvEtMonthYear, tvEnDate, tvPlaceholder;

    // “July 13, 2025”
    private static final DateTimeFormatter GREG_FORMAT =
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault());

    // Amharic months 1–13 (with transliteration)
    private static final String[] ETHIOPIC_MONTHS = {
            "መስከረም (Meskerem)",
            "ጥቅምት (Tikimt)",
            "ህዳር (Hidar)",
            "ታኅሣሥ (Tahsas)",
            "ጥር (Tir)",
            "የካቲት (Yekatit)",
            "መጋቢት (Megabit)",
            "ሚያዝያ (Miyazya)",
            "ግንቦት (Ginbot)",
            "ሰኔ (Sene)",
            "ሐምሌ (Hamle)",
            "ነሐሴ (Nehasse)",
            "ጳጉሜን (Pagumen)"
    };

    // Amharic days of week Monday–Sunday
    private static final String[] ETHIOPIC_DAYS = {
            "ሰኞ (Segno)",
            "ማክሰኞ (Maksegno)",
            "እሮብ (Erob)",
            "ሐሙስ (Hamus)",
            "ዓርብ (Arb)",
            "ቅዳሜ (Kidame)",
            "እሑድ (Ehud)"
    };

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup ctr,
                             @Nullable Bundle savedInstanceState) {
        return inf.inflate(R.layout.fragment_calendar, ctr, false);
    }

    @Override public void onViewCreated(@NonNull View view,
                                        @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // bind views
        tvEtDayName   = view.findViewById(R.id.tvEtDayName);
        tvEtDayNumber = view.findViewById(R.id.tvEtDayNumber);
        tvEtMonthYear = view.findViewById(R.id.tvEtMonthYear);
        tvEnDate      = view.findViewById(R.id.tvEnDate);
        tvPlaceholder = view.findViewById(R.id.tvPlaceholder);               // new :contentReference[oaicite:0]{index=0}
        calendarView  = view.findViewById(R.id.calendarView);

        // tap a date → update header + hide placeholder
        calendarView.setOnDateChangedListener((w, d, sel) -> {
            updateForDate(d);
            tvPlaceholder.setVisibility(View.GONE);
        });

        // swipe month → re‑draw header for the currently selected (or first) day
        calendarView.setOnMonthChangedListener((w, month) -> {
            CalendarDay sel = calendarView.getSelectedDate();
            updateForDate(sel != null ? sel : month);
        });

        // initial state: today
        CalendarDay today = CalendarDay.today();
        calendarView.setCurrentDate(today);
        calendarView.setSelectedDate(today);
        updateForDate(today);
    }

    private void updateForDate(@NonNull CalendarDay date) {
        // 1) Gregorian header
            // CORRECT: add 1 because getMonth() returns 0–11
                    int gregMonth = date.getMonth() + 1;
            LocalDate greg = LocalDate.of(
                        date.getYear(),
                        gregMonth,
                        date.getDay()
                            );
        tvEnDate.setText(greg.format(GREG_FORMAT));

        // 2) Ethiopian ISO “yyyy‑MM‑dd”
            String ethIso = DateUtils.convertGregorianToEthiopian(
                        date.getYear(), gregMonth, date.getDay()
                            );
        String[] parts = ethIso.split("\\D+");
        if (parts.length < 3) {
            Log.e("CalendarFragment", "Bad ETH date: " + ethIso);
            tvEtDayName.setText("");
            tvEtDayNumber.setText("");
            tvEtMonthYear.setText("");
            return;
        }
        int eY = Integer.parseInt(parts[0]);
        int eM = Integer.parseInt(parts[1]);
        int eD = Integer.parseInt(parts[2]);

        // 3) Ethiopian DOW (Mon=1…Sun=7 → idx 0–6)
        int dowIdx = greg.getDayOfWeek().getValue() - 1;
        tvEtDayName.setText(ETHIOPIC_DAYS[dowIdx]);

        // 4) Day number & month/year
        tvEtDayNumber.setText(String.valueOf(eD));
        tvEtMonthYear.setText(ETHIOPIC_MONTHS[eM - 1] + " " + eY);
    }
}
