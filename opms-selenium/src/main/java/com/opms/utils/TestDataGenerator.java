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
}
