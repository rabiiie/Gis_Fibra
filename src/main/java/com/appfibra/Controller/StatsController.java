package com.appfibra.Controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.*;

@RestController
@RequestMapping("/api/civil-works")
public class StatsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/stats")
    public Map<String, Double> getStats(@RequestBody Map<String, Object> payload) {
        String dimension = Objects.toString(payload.get("dimension"), "type"); // Si no hay "dimension", usa "type"
        Map<String, String> filters = (Map<String, String>) payload.getOrDefault("filters", new HashMap<>());

        // Construcción de la consulta SQL
        StringBuilder sql = new StringBuilder(
            "SELECT COALESCE(" + dimension + ", 'Unknown') AS dim, " +
            "       SUM(cw.length_meters) AS total_meters " +
            "FROM civil_works cw " +
            "LEFT JOIN streets s ON cw.street_id = s.street_id " +
            "LEFT JOIN dps dp2 ON cw.dp_id = dp2.dp_id " +
            "LEFT JOIN pops pop2 ON dp2.pop_id = pop2.pop_id " +
            "LEFT JOIN projects p ON pop2.project_id = p.project_id " +
            "LEFT JOIN clients c ON p.client_id = c.client_id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        // Aplicar filtros seguros evitando NullPointerException
        if (!Objects.toString(filters.get("client"), "").isEmpty()) {
			sql.append(" AND c.name ILIKE ? ");
			params.add("%" + filters.get("client") + "%");
		}
        if (!Objects.toString(filters.get("project"), "").isEmpty()) {
            sql.append(" AND p.name ILIKE ? ");
            params.add("%" + filters.get("project") + "%");
        }
        if (!Objects.toString(filters.get("pop"), "").isEmpty()) {
            sql.append(" AND pop2.name ILIKE ? ");
            params.add("%" + filters.get("pop") + "%");
        }
        if (!Objects.toString(filters.get("dp"), "").isEmpty()) {
            sql.append(" AND dp2.name ILIKE ? ");
            params.add("%" + filters.get("dp") + "%");
        }
        if (!Objects.toString(filters.get("street"), "").isEmpty()) {
            sql.append(" AND s.name ILIKE ? ");
            params.add("%" + filters.get("street") + "%");
        }

        // Agrupar por dimensión
        sql.append(" GROUP BY ").append(dimension).append(" ORDER BY SUM(cw.length_meters) DESC ");

        // Ejecutar consulta
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());

        // Convertir resultado a Map<String, Double>
        Map<String, Double> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object dimVal = row.get("dim");
            Object sumVal = row.get("total_meters");
            if (dimVal != null && sumVal != null) {
                result.put(dimVal.toString(), ((Number) sumVal).doubleValue());
            }
        }

        return result;
    }
    
    @GetMapping("/by-type")
    public ResponseEntity<Map<String, Integer>> getCivilWorksByType() {
        String sql = "SELECT type, SUM(length_meters) as total FROM civil_works GROUP BY type";
        
        Map<String, Integer> result = jdbcTemplate.query(sql, rs -> {
            Map<String, Integer> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getString("type"), rs.getInt("total"));
            }
            return map;
        });

        return ResponseEntity.ok(result);
    }

}
