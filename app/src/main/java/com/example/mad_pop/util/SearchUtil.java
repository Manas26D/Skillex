package com.example.mad_pop.util;

public final class SearchUtil {
    private SearchUtil() {}

    public static boolean matches(String query, String... values) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String q = query.trim().toLowerCase();
        for (String value : values) {
            if (value != null && value.toLowerCase().contains(q)) {
                return true;
            }
        }
        return false;
    }
}

