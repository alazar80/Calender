package com.example.calender.util;


import org.joda.time.DateTime;
import org.joda.time.chrono.EthiopicChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.chrono.ISOChronology;

public class DateUtils {
    private static final DateTimeFormatter F = DateTimeFormat.forPattern("yyyy‑MM‑dd");

    // Ethiopian → Gregorian
    public static String convertEthiopianToGregorian(int y, int m, int d) {
        DateTime eth = new DateTime(y, m, d, 0, 0, EthiopicChronology.getInstanceUTC());
        DateTime iso = eth.withChronology(ISOChronology.getInstanceUTC());
        return iso.toString(F);
    }

    // Gregorian → Ethiopian
    public static String convertGregorianToEthiopian(int y, int m, int d) {
        DateTime iso = new DateTime(y, m, d, 0, 0, ISOChronology.getInstanceUTC());
        DateTime eth = iso.withChronology(EthiopicChronology.getInstanceUTC());
        return eth.toString(F);
    }
}
