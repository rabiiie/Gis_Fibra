package com.appfibra.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QueryService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public QueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // Para Date/Time
    }

    /**
     * Ejecuta una consulta SQL y devuelve los resultados en formato JSON prettificado.
     */
    public String executeQuery(String sqlQuery) {
        // Chequeo simple: debe iniciar con SELECT y no tener ;
        String check = sqlQuery.toLowerCase().trim();
        if (!check.startsWith("select") || check.contains(";")) {
            throw new IllegalArgumentException("Se requiere una única sentencia SELECT (sin punto y coma):\n" + sqlQuery);
        }

        return jdbcTemplate.query(sqlQuery, rs -> {
            List<Map<String, Object>> results = new ArrayList<>();
            var metaData = rs.getMetaData();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }

            try {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(results);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error al formatear los resultados", e);
            }
        });
    }
}
