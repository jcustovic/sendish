package com.sendish.api.util;

public class StringUtils {

    public static String trim(String string, int maxLength) {
        if (string.length() > maxLength) {
            return org.apache.commons.lang3.StringUtils.left(string, maxLength - 3) + "...";
        } else {
            return string;
        }
    }

}
