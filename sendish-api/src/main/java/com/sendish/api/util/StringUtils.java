package com.sendish.api.util;

public class StringUtils {

    public static String trim(String string, int maxLength) {
        return trim(string, maxLength, null);
    }

    public static String trim(String string, int maxLength, String suffix) {
        if (string.length() > maxLength) {
            if (suffix == null) {
                return org.apache.commons.lang3.StringUtils.left(string, maxLength);
            } else {
                return org.apache.commons.lang3.StringUtils.left(string, maxLength - suffix.length()) + suffix;
            }
        } else {
            return string;
        }
    }

}
