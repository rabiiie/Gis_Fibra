package com.appfibra.service;

import org.locationtech.jts.geom.Point;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class GeoService {

    private final JdbcTemplate jdbcTemplate;

    public GeoService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getSpatialContext(Point location) {
        // Ejemplo: Obtener información de 1km alrededor
        String query = """
            SELECT ST_AsGeoJSON(ST_Buffer(?, 1000)) AS area,
                   COUNT(*) AS lugares_interes 
            FROM puntos_interes 
            WHERE ST_DWithin(geom, ?, 1000)
        """;

        return jdbcTemplate.queryForObject(query, (rs, rowNum) -> 
            """
            Área de interés: %s
            Lugares relevantes: %d
            """.formatted(rs.getString("area"), rs.getInt("lugares_interes")),
            location, location
        );
    }
}
