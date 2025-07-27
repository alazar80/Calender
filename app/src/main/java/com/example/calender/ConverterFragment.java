package com.example.calender;

import static java.security.AccessController.getContext;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.calender.util.DateUtils;

import java.util.Calendar;

public class ConverterFragment extends Fragment {
    private RadioGroup rgType;
    private Button btnPick;
    private TextView tvResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        return i.inflate(R.layout.fragment_converter, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, Bundle s) {
        super.onViewCreated(v, s);
        rgType = v.findViewById(R.id.rgType);
        btnPick = v.findViewById(R.id.btnPick);
        tvResult = v.findViewById(R.id.tvResult);

        btnPick.setOnClickListener(x -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(getContext(),
                    (dp, y, m, d) -> {
                        boolean etoG = rgType.getCheckedRadioButtonId()==R.id.rbEtoG;
                        String out = etoG
                                ? DateUtils.convertEthiopianToGregorian(y, m+1, d)
                                : DateUtils.convertGregorianToEthiopian(y, m+1, d);
                        tvResult.setText(out);
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }
}

