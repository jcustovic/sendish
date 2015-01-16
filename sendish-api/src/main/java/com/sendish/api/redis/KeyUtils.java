package com.sendish.api.redis;

public final class KeyUtils {

    private KeyUtils() {
        // Final class
    }

    private static final String USER = "user:";
    private static final String PHOTO = "photo:";
    private static final String RANK = "rank:";

    /**
     * Hash holding stat properties for photo
     */
    public static String photoStatistics(long photoId) {
        return PHOTO + photoId + ":stat";
    }

    /**
     * Set holding all the cities that photo has been in
     */
    public static String photoCities(long photoId) {
        return PHOTO + photoId + ":cities";
    }

    /**
     * Hash holding stat properties for user
     */
    public static String userStatistics(long userId) {
        return USER + userId + ":stat";
    }

    /**
     * Set holding all the cities users photos have traveled
     */
    public static String userCities(long userId) {
        return USER + userId + ":cities";
    }

    /**
     * Set holding received photos by user
     */
    public static String userReceivedPhotos(long userId) {
        return USER + userId + ":photo:received";
    }

    /**
     * Sorted set of users sorted by points.
     */
    public static String globalRanking() {
        return RANK + ":global";
    }

}
