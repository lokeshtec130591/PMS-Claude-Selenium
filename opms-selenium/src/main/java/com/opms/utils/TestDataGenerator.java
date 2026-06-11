package com.opms.utils;

import java.util.Random;

public class TestDataGenerator {

    private static final Random random = new Random();

    public static String generateUniqueString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append((char) (random.nextInt(26) + 'a'));
        }
        return sb.toString();
    }

    public static String generateUniqueEmail() {
        return "user" + generateUniqueString() + "@selautomation.com";
    }

    /** Generates a 10-digit US phone number string e.g. 5123456789 */
    public static String generatePhoneNumber() {
        return "5" + String.format("%09d", random.nextInt(1_000_000_000));
    }

    /**
     * Generates a valid DOB string in MMDDYYYY order (no separators).
     * Matches the datepicker placeholder MM/DD/YYYY — segments auto-advance.
     */
    public static String generateDOB() {
        int year  = 1970 + random.nextInt(25);   // 1970–1994
        int month = 1 + random.nextInt(12);
        int day   = 1 + random.nextInt(28);
        return String.format("%02d%02d%04d", month, day, year);
    }
}
