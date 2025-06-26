package com.appfibra.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/contracts")
public class ContractsSuggestionsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/suggestions")
    public List<String> getSuggestions(@RequestParam String column, @RequestParam(required = false) String term) {
        List<String> allowedColumns = List.of(
            "subtype",
            "access_location",
            "aufgabenstatus",
            "building_status",
            "building_hausbegehung_status",
            "building_action_holder",
            "unit_status",
            "unit_action_holder",
            "unit_hausbegehung_status",
            "anschlussstatus",
            "activeoperator_name_from_tri",
            "outsource_name"
        );

        if (!allowedColumns.contains(column)) {
            return Collections.emptyList();
        }

        String dbField = switch (column) {
            case "subtype" -> "c.subtype";
            case "access_location" -> "c.Access_location";
            case "aufgabenstatus" -> "c.AufgabenStatus";
            case "building_status" -> "b.status_1";
            case "building_hausbegehung_status" -> "b.status_hausbegehung_hvs_from_house";
            case "building_action_holder" -> "b.action_holder";
            case "unit_status" -> "u.status_1";
            case "unit_action_holder" -> "u.action_holder";
            case "unit_hausbegehung_status" -> "u.status_hausbegehung";
            case "anschlussstatus" -> "m.anschlussstatus";
            case "activeoperator_name_from_tri" -> "m.activeoperator_name_from_tri";
            case "outsource_name" -> "o.outsource_name";
            default -> throw new IllegalArgumentException("Campo no válido: " + column);
        };

        String sql;
        List<String> results;

        if (term == null || term.isBlank()) {
            sql = String.format("""
                SELECT %s AS val
                FROM contracts_list c
                LEFT JOIN task_buildings b ON c.building_id = b.assignment_number
                LEFT JOIN task_units u ON b.assignment_number = u.building_id
                LEFT JOIN mastergrid m ON m.Auftragsnummer = u.assignment_number
                LEFT JOIN outsources o ON LOWER(c.access_location) = LOWER(o.AP)
                GROUP BY %s
                ORDER BY COUNT(*) DESC
                LIMIT 4
            """, dbField, dbField);
            results = jdbcTemplate.queryForList(sql, String.class);
        } else {
            sql = String.format("""
                SELECT DISTINCT %s AS val
                FROM contracts_list c
                LEFT JOIN task_buildings b ON c.building_id = b.assignment_number
                LEFT JOIN task_units u ON b.assignment_number = u.building_id
                LEFT JOIN mastergrid m ON m.Auftragsnummer = u.assignment_number
                LEFT JOIN outsources o ON LOWER(c.access_location) = LOWER(o.AP)
                WHERE %s ILIKE ?
                ORDER BY val ASC
                LIMIT 10
            """, dbField, dbField);
            results = jdbcTemplate.queryForList(sql, String.class, "%" + term + "%");
        }

        return results.isEmpty() ? Collections.emptyList() : results;
    }

} 
