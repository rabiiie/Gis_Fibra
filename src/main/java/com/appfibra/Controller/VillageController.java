package com.appfibra.Controller;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;


@RestController
@RequestMapping("/api/village")
public class VillageController {

    private final JdbcTemplate jdbcTemplate;

    public VillageController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 🔹 Devuelve la ubicación de un village basado en su nombre.
     */
    @GetMapping("/{villageName}/location")
    public Map<String, Object> getVillageLocation(@PathVariable String villageName) {
        // Asegurar que el villageName no tenga caracteres peligrosos
        villageName = villageName.replaceAll("[^a-zA-Z0-9_-]", "");  

        String tableName = normalizeTableName(villageName);  // 🔹 Convertir "Fürstenau" a "Furstenau"
        String sql = """
            SELECT ? AS village, 
                   ST_X(ST_Transform(ST_Centroid(ST_Union(geom)), 4326)) AS lon, 
                   ST_Y(ST_Transform(ST_Centroid(ST_Union(geom)), 4326)) AS lat
            FROM "dgf"."%s"
            WHERE village = ?
        """.formatted(tableName);


        try {
            return jdbcTemplate.queryForMap(sql, villageName, villageName);
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Village not found");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Internal server error");
            response.put("details", e.getMessage());
            return response;
        }
    }
    
    public String normalizeTableName(String villageName) {
        return Normalizer.normalize(villageName, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");  // 🔹 Elimina caracteres no ASCII como ü
    }
    
    @GetMapping("/all-civil-works")
    public ResponseEntity<Map<String, Object>> getAllSchemasCivilWorks() {
        String sql = """
            SELECT schemaname, tablename 
            FROM pg_tables 
            WHERE schemaname IN ('dgf', 'ugg')
        """;

        List<Map<String, Object>> tables = jdbcTemplate.queryForList(sql);
        List<Map<String, Object>> features = new ArrayList<>();

        for (Map<String, Object> table : tables) {
            String schema = (String) table.get("schemaname");
            String tableName = (String) table.get("tablename");
            
            String query = """
                SELECT ?, ?, ST_AsGeoJSON(ST_Transform(geom, 4326)) AS geometry
                FROM "%s"."%s"
            """.formatted(schema, tableName);
            
            try {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(query, schema, tableName);
                for (Map<String, Object> row : results) {
                    Map<String, Object> feature = new HashMap<>();
                    feature.put("type", "Feature");
                    feature.put("geometry", new ObjectMapper().readTree((String) row.get("geometry")));

                    Map<String, Object> properties = new HashMap<>();
                    properties.put("schema", schema);
                    properties.put("table", tableName);
                    feature.put("properties", properties);

                    features.add(feature);
                }
            } catch (Exception e) {
                System.err.println("❌ Error al obtener datos de " + schema + "." + tableName + ": " + e.getMessage());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("type", "FeatureCollection");
        response.put("features", features);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
    }

}
