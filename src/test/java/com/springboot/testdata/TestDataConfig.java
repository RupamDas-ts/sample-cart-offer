package com.springboot.testdata;

import java.util.Properties;

public class TestDataConfig {
    private static final Properties config = new Properties();

    static {
        // Default configuration
        config.setProperty("test.data.base.restaurant.id", "1000");
        config.setProperty("test.data.default.cart.value", "200");
        config.setProperty("test.data.timeout.seconds", "10");
    }

    public static int getBaseRestaurantId() {
        return Integer.parseInt(config.getProperty("test.data.base.restaurant.id", "1000"));
    }

    public static int getTimeoutSeconds() {
        return Integer.parseInt(config.getProperty("test.data.timeout.seconds", "10"));
    }

    // Method to generate unique restaurant IDs for tests
    public static int generateUniqueRestaurantId() {
        return getBaseRestaurantId() + (int) (Math.random() * 1000);
    }
}
