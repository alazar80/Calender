package com.example.calender.util;

import org.joda.time.DateTime;
import org.joda.time.chrono.EthiopicChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.chrono.ISOChronology;

public final class DateUtils {
    private static final DateTimeFormatter F = DateTimeFormat.forPattern("yyyy-MM-dd");

    private DateUtils() {
        // Utility class.
    }

    /**
     * Converts a valid Ethiopian date to Gregorian using Joda-Time's
     * Ethiopic chronology. Invalid dates are rejected rather than silently
     * producing an incorrect result.
     */
    public static String convertEthiopianToGregorian(int y, int m, int d) {
        validateEthiopianDate(y, m, d);
        DateTime eth = new DateTime(y, m, d, 0, 0, EthiopicChronology.getInstanceUTC());
        DateTime iso = eth.withChronology(ISOChronology.getInstanceUTC());
        return iso.toString(F);
    }

    /**
     * Converts a valid Gregorian date to Ethiopian.
     */
    public static String convertGregorianToEthiopian(int y, int m, int d) {
        validateGregorianDate(y, m, d);
        DateTime iso = new DateTime(y, m, d, 0, 0, ISOChronology.getInstanceUTC());
        DateTime eth = iso.withChronology(EthiopicChronology.getInstanceUTC());
        return eth.toString(F);
    }

    private static void validateEthiopianDate(int year, int month, int day) {
        if (year < 1) {
            throw new IllegalArgumentException("Ethiopian year must be positive");
        }
        if (month < 1 || month > 13) {
            throw new IllegalArgumentException("Ethiopian month must be between 1 and 13");
        }
        int maxDay = month == 13 ? (year % 4 == 3 ? 6 : 5) : 30;
        if (day < 1 || day > maxDay) {
            throw new IllegalArgumentException("Invalid Ethiopian day for the selected month/year");
        }
    }

    private static void validateGregorianDate(int year, int month, int day) {
        if (year < 1) {
            throw new IllegalArgumentException("Gregorian year must be positive");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Gregorian month must be between 1 and 12");
        }
        int maxDay;
        switch (month) {
            case 2:
                maxDay = isGregorianLeapYear(year) ? 29 : 28;
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                maxDay = 30;
                break;
            default:
                maxDay = 31;
                break;
        }
        if (day < 1 || day > maxDay) {
            throw new IllegalArgumentException("Invalid Gregorian day for the selected month/year");
        }
    }

    private static boolean isGregorianLeapYear(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }
}
