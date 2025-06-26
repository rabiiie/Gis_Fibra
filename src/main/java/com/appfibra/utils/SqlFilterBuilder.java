package com.appfibra.utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.LocalDate;

/**
 * SqlFilterBuilder permite construir cláusulas WHERE dinámicas en SQL,
 * soportando:
 *  - filtros de texto (ILIKE/NOT ILIKE) sobre cualquier columna (se castea a texto)
 *  - filtros exactos de texto (IN/equal)
 *  - filtros numéricos (equal/IN)
 *  - filtros de fecha exactos y rangos (addDateFilter)
 *  - filtros de semanas ISO (Wxx, Wxx-Wyy, YYYY-Wxx, YYYY-Wxx-Wyy)
 */
public class SqlFilterBuilder {
    private final List<String> whereClauses = new ArrayList<>();
    private final List<Object> params       = new ArrayList<>();

    /**
     * Filtro de texto con comodines (%valor%), admite exclusiones (!valor).
     * Siempre castea la columna a texto para permitir ILIKE sobre cualquier tipo.
     * Ejemplos:
     *   raw = "abc"       → WHERE column::text ILIKE '%abc%'
     *   raw = "!xyz"      → WHERE column::text NOT ILIKE '%xyz%'
     *   raw = "a,b,!c"    → WHERE (column::text ILIKE '%a%' OR column::text ILIKE '%b%')
     *                         AND (column::text NOT ILIKE '%c%')
     */
    public void addTextFilter(String column, String rawValue) {
        if (rawValue == null || rawValue.isBlank()) return;

        List<String> values = parseValues(rawValue);
        String colExpr = "CAST(" + column + " AS VARCHAR)";


        // Caso exclusión única '!valor'
        if (values.size() == 1 && values.get(0).startsWith("!")) {
            String v = values.get(0).substring(1).trim();
            if (!v.isEmpty()) {
                whereClauses.add(colExpr + " NOT ILIKE ?");
                params.add("%" + v + "%");
            }
            return;
        }

        // Separar positivos y negativos
        List<String> positives = values.stream()
            .filter(v -> !v.startsWith("!"))
            .map(String::trim).filter(v -> !v.isEmpty())
            .collect(Collectors.toList());

        List<String> negatives = values.stream()
            .filter(v -> v.startsWith("!"))
            .map(v -> v.substring(1).trim())
            .filter(v -> !v.isEmpty())
            .collect(Collectors.toList());

        if (!positives.isEmpty()) {
            String cond = positives.stream()
                .map(v -> colExpr + " ILIKE ?")
                .collect(Collectors.joining(" OR "));
            whereClauses.add("(" + cond + ")");
            positives.forEach(v -> params.add("%" + v + "%"));
        }
        if (!negatives.isEmpty()) {
            String cond = negatives.stream()
                .map(v -> colExpr + " NOT ILIKE ?")
                .collect(Collectors.joining(" AND "));
            whereClauses.add("(" + cond + ")");
            negatives.forEach(v -> params.add("%" + v + "%"));
        }
    }

    /**
     * Filtro de texto exacto (sin comodines). Usa '=' o 'IN'.
     *   raw = "v1"     → WHERE column = 'v1'
     *   raw = "v1,v2" → WHERE column IN ('v1','v2')
     */
    public void addExactTextFilter(String column, String rawValue) {
        if (rawValue == null || rawValue.isBlank()) return;
        List<String> values = parseValues(rawValue);
        if (values.size() == 1) {
            whereClauses.add(column + " = ?");
            params.add(values.get(0));
        } else {
            String ph = values.stream().map(v -> "?").collect(Collectors.joining(", "));
            whereClauses.add(column + " IN (" + ph + ")");
            params.addAll(values);
        }
    }

    /**
     * Filtro numérico. Usa '=' o 'IN'.
     *   raw = "123"    → WHERE column = 123
     *   raw = "1,2,3" → WHERE column IN (1,2,3)
     */
    public void addNumericFilter(String column, String rawValue) {
        if (rawValue == null || rawValue.isBlank()) return;
        List<String> values = parseValues(rawValue);
        try {
            if (values.size() == 1) {
                whereClauses.add(column + " = ?");
                params.add(Integer.parseInt(values.get(0)));
            } else {
                String ph = values.stream().map(v -> "?").collect(Collectors.joining(", "));
                whereClauses.add(column + " IN (" + ph + ")");
                for (String v : values) {
                    params.add(Integer.parseInt(v));
                }
            }
        } catch (NumberFormatException ex) {
            System.err.println("⚠️ Valor numérico inválido: " + rawValue);
        }
    }

    /**
     * Filtro de fecha exacta o rango de fechas.
     *   value = "YYYY-MM-DD"              → WHERE column = DATE
     *   value = "YYYY-MM-DD,YYYY-MM-DD" → WHERE column BETWEEN DATE1 AND DATE2
     */
    public void addDateFilter(String column, String value) {
        if (value == null || value.isBlank()) return;
        try {
            if (value.contains(",")) {
                String[] parts = value.split(",");
                if (parts.length == 2) {
                    whereClauses.add(column + " BETWEEN ? AND ?");
                    params.add(LocalDate.parse(parts[0].trim()));
                    params.add(LocalDate.parse(parts[1].trim()));
                }
            } else {
                whereClauses.add(column + " = ?");
                params.add(LocalDate.parse(value.trim()));
            }
        } catch (Exception ex) {
            System.err.println("⚠️ Fecha inválida para columna " + column + ": " + value);
        }
    }

    /**
     * Filtro de semanas ISO, soporta rangos y años explícitos.
     * rawValue ejemplos: "W01", "W01-W02", "2025-W01", "2025-W01-W02", "W01,W03-05,2025-W10"
     * isTextDate = false para columnas DATE/TIMESTAMP;
     *               true para cadenas con formato 'YYYY-MM-DD' o 'YYYY-MM-DD HH24:MI:SS'.
     */
    public void addWeekRangeFilter(String column, String rawValue, boolean isTextDate) {
        if (rawValue == null || rawValue.isBlank()) return;
        rawValue = rawValue.trim().toUpperCase();

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Map<Integer, Set<Integer>> yearWeekMap = new LinkedHashMap<>();

        try {
            // Construir mapa año → semanas
            for (String part : rawValue.split(",")) {
                part = part.trim();
                if (!part.contains("W")) continue;

                int year = currentYear;
                String weekPart = part;

                if (part.matches("\\d{4}-W\\d{1,2}(-W\\d{1,2})?")) {
                    year = Integer.parseInt(part.substring(0, 4));
                    weekPart = part.substring(5);
                } else if (part.startsWith("W")) {
                    weekPart = part.substring(1);
                }

                yearWeekMap.putIfAbsent(year, new TreeSet<>());
                Set<Integer> weeks = yearWeekMap.get(year);

                if (weekPart.contains("-")) {
                    String[] rng = weekPart.replaceAll("W", "").split("-");
                    int start = Integer.parseInt(rng[0].replaceFirst("^0+(?!$)", ""));
                    int end   = Integer.parseInt(rng[1].replaceFirst("^0+(?!$)", ""));
                    for (int w = start; w <= end; w++) weeks.add(w);
                } else {
                    int w = Integer.parseInt(weekPart.replaceFirst("^0+(?!$)", ""));
                    weeks.add(w);
                }
            }
        } catch (Exception ex) {
            System.err.println("⚠️ Filtro de semana inválido: " + rawValue);
            return;
        }

        StringBuilder cond = new StringBuilder();
        for (var entry : yearWeekMap.entrySet()) {
            int year   = entry.getKey();
            Set<Integer> weeks = entry.getValue();
            if (weeks.isEmpty()) continue;

            String weekExpr = isTextDate
                ? "EXTRACT(WEEK FROM TO_TIMESTAMP(" + column + ", 'YYYY-MM-DD HH24:MI:SS'))"
                : "EXTRACT(WEEK FROM " + column + ")";
            String yearExpr = isTextDate
                ? "EXTRACT(YEAR FROM TO_TIMESTAMP(" + column + ", 'YYYY-MM-DD HH24:MI:SS'))"
                : "EXTRACT(YEAR FROM " + column + ")";

            String ph = weeks.stream().map(w -> "?").collect(Collectors.joining(", "));
            cond.append("(")
                .append(weekExpr).append(" IN (").append(ph).append(")")
                .append(" AND ").append(yearExpr).append(" = ?")
                .append(") OR ");

            params.addAll(weeks);
            params.add(year);
        }

        if (cond.length() > 4) {
            cond.setLength(cond.length() - 4); // quita último " OR "
            whereClauses.add("(" + cond + ")");
        }
    }

    /**
     * Devuelve copia de la lista de parámetros.
     */
    public List<Object> getParams() {
        return List.copyOf(params);
    }

    /**
     * Construye la cláusula WHERE completa (o cadena vacía si no hay filtros).
     */
    public String buildWhereClause() {
        List<String> valid = whereClauses.stream()
            .map(String::trim)
            .filter(c -> !c.isEmpty() && !c.equals("()"))
            .collect(Collectors.toList());
        if (valid.isEmpty()) {
            return "";
        }
        return " WHERE " + String.join(" AND ", valid);
    }

    private List<String> parseValues(String raw) {
        return Stream.of(raw.split(","))
            .map(String::trim)
            .filter(v -> !v.isEmpty())
            .collect(Collectors.toList());
    }
}