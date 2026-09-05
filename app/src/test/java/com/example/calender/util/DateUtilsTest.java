package com.example.calender.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DateUtilsTest {
    @Test
    public void ethiopianToGregorianUsesAsciiDateSeparator() {
        String result = DateUtils.convertEthiopianToGregorian(2018, 1, 1);
        assertEquals('-', result.charAt(4));
        assertEquals('-', result.charAt(7));
        assertEquals(10, result.length());
    }

    @Test
    public void gregorianToEthiopianUsesAsciiDateSeparator() {
        String result = DateUtils.convertGregorianToEthiopian(2025, 9, 11);
        assertEquals('-', result.charAt(4));
        assertEquals('-', result.charAt(7));
        assertEquals(10, result.length());
    }

    @Test
    public void conversionRoundTripPreservesDate() {
        String ethiopian = "2018-01-01";
        String gregorian = DateUtils.convertEthiopianToGregorian(2018, 1, 1);
        String[] parts = gregorian.split("-");

        String convertedBack = DateUtils.convertGregorianToEthiopian(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2])
        );

        assertEquals(ethiopian, convertedBack);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsGregorianMonthOutsideRange() {
        DateUtils.convertGregorianToEthiopian(2025, 13, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidGregorianDay() {
        DateUtils.convertGregorianToEthiopian(2025, 2, 29);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEthiopianMonthOutsideRange() {
        DateUtils.convertEthiopianToGregorian(2018, 14, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidPagumenDay() {
        DateUtils.convertEthiopianToGregorian(2017, 13, 6);
    }
}
