package com.opms.utils;

import java.util.Random;

public class TestDataGenerator {

    public static String generateUniqueString() {
        StringBuilder uniqueString = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            char randomChar = (char) (random.nextInt(26) + 'a');
            uniqueString.append(randomChar);
        }
        return uniqueString.toString();
    }

    public static String generateUniqueEmail() {
        return "user" + generateUniqueString() + "@selautomation.com";
    }

    /** Generates a 10-digit US phone number string e.g. 5123456789 */
    public static String generatePhoneNumber() {
        Random random = new Random();
        return "5" + String.format("%09d", random.nextInt(1_000_000_000));
    }

    private static final String ALPHANUMERIC_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /** Generates a random alphanumeric string of the given length, e.g. for member/group plan IDs. */
    public static String generateAlphanumeric(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC_CHARS.charAt(random.nextInt(ALPHANUMERIC_CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * Generates a valid DOB string in MMDDYYYY order (no separators).
     * Matches the datepicker placeholder MM/DD/YYYY — segments auto-advance.
     */
    public static String generateDOB() {
        Random random = new Random();
        int year  = 1970 + random.nextInt(25);
        int month = 1 + random.nextInt(12);
        int day   = 1 + random.nextInt(28);
        return String.format("%02d%02d%04d", month, day, year);
    }
}
