package com.appfibra.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qgis")
public class QGISController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/geojson")
    public ResponseEntity<String> getGeoJSON() {
        String sql = """
            SELECT jsonb_build_object(
                'type', 'FeatureCollection',
                'features', jsonb_agg(
                    jsonb_build_object(
                        'type', 'Feature',
                        'geometry', ST_AsGeoJSON(geom)::jsonb,
                        'properties', to_jsonb(t) - 'geom'
                    )
                )
            ) AS geojson
            FROM civil_works t;
        """;

        String geoJson = jdbcTemplate.queryForObject(sql, String.class);
        return ResponseEntity.ok(geoJson);
    }
}
