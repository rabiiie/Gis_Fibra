package com.appfibra.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/civil-works")
public class SuggestionsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/suggestions")
    public List<String> getSuggestions(@RequestParam String column, @RequestParam(required = false) String term) {
        List<String> allowedColumns = List.of("project", "pop", "dp", "street", "tzip", "type", "spec", "length_meters", "village");
        
        if (!allowedColumns.contains(column)) {
            return Collections.emptyList();
        }

        String dbField;
        switch (column) {
            case "project": dbField = "p.name"; break;
            case "pop": dbField = "pop2.name"; break;  
            case "dp": dbField = "dp2.name"; break;  
            case "street": dbField = "s.name"; break;  
            case "tzip": dbField = "s.tzip"; break;
            case "type": dbField = "cw.type"; break;
            case "spec": dbField = "cw.spec"; break;
            case "length_meters": dbField = "CAST(cw.length_meters AS CHAR)"; break;
            case "village": dbField = "s.village"; break;
            default: return Collections.emptyList();
        }

        String sql;
        List<String> results;

        if (term == null || term.isBlank()) {
            // 🔹 Si el usuario no ha escrito nada, mostrar los 4 valores más comunes en la base de datos
        	sql = "SELECT " + dbField + " AS val " +
                    "FROM civil_works cw " +
                    "LEFT JOIN streets s ON cw.street_id = s.street_id " +
                    "LEFT JOIN dps dp2 ON cw.dp_id = dp2.dp_id " +
                    "LEFT JOIN pops pop2 ON dp2.pop_id = pop2.pop_id " +
                    "LEFT JOIN projects p ON pop2.project_id = p.project_id " +
                    "GROUP BY " + dbField + " " +
                    "ORDER BY COUNT(*) DESC " +
                    "LIMIT 4";
            results = jdbcTemplate.queryForList(sql, String.class);
        } else {
            // 🔹 Cuando el usuario empieza a escribir, ordenar por relevancia
        	sql = "SELECT DISTINCT " + dbField + " AS val " +
                    "FROM civil_works cw " +
                    "LEFT JOIN streets s ON cw.street_id = s.street_id " +
                    "LEFT JOIN dps dp2 ON cw.dp_id = dp2.dp_id " +
                    "LEFT JOIN pops pop2 ON dp2.pop_id = pop2.pop_id " +
                    "LEFT JOIN projects p ON pop2.project_id = p.project_id " +
                    "WHERE " + dbField + " ILIKE ? " +
                    "ORDER BY val ASC " +
                    "LIMIT 10";
            results = jdbcTemplate.queryForList(sql, String.class, "%" + term + "%");
        }

        return results.isEmpty() ? Collections.emptyList() : results;
    }
}
