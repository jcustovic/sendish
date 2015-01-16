package com.sendish.api.redis;

public final class KeyUtils {

    private KeyUtils() {
        // Final class
    }

    private static final String USER = "user:";
    private static final String PHOTO = "photo:";

    public static String photoStatistics(long photoId) {
        return PHOTO + photoId + ":stat";
    }

    public static String userStatistics(long userId) {
        return USER + userId + ":stat";
    }

}
