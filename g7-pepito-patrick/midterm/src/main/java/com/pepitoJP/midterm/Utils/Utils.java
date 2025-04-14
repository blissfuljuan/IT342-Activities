package com.pepitoJP.midterm.Utils;

public class Utils {

    /**
     * Splits a full name into first name and last name.
     * If the full name is null or empty, returns an array with two empty strings.
     *
     * @param fullName the full name to split
     * @return a String array where index 0 is the  first name and index 1 is the last name
     */
    public static String[] splitFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[] {"", ""};
        }
        String[] names = fullName.trim().split(" ", 2);
        String firstName = names[0];
        String lastName = names.length > 1 ? names[1] : "";
        return new String[]{firstName, lastName};
    }

    /**
     * Checks if a string is null or empty after trimming.
     *
     * @param s the string to check
     * @return true if the string is null or empty; otherwise, false
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Safely converts an object to a string.
     *
     * @param obj the object to convert
     * @return an empty string if the object is null; otherwise, the object's string representation
     */
    public static String safeToString(Object obj) {
        return obj == null ? "" : obj.toString();
    }
}