package com.appfibra.utils;

import java.util.regex.Pattern;

public class SqlInjectionValidator {
    private static final Pattern FORBIDDEN_KEYWORDS = Pattern.compile(
        "\\b(DROP|DELETE|INSERT|UPDATE|TRUNCATE|EXEC|ALTER|CREATE|REVOKE|GRANT)\\b",
        Pattern.CASE_INSENSITIVE
    );

    public static void validate(String query) {
        if (FORBIDDEN_KEYWORDS.matcher(query).find()) {
            throw new SecurityException("Operación SQL no permitida");
        }
        
        if (!query.toUpperCase().startsWith("SELECT")) {
            throw new SecurityException("Solo se permiten consultas SELECT");
        }
    }
}