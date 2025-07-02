package com.appfibra.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class BuildingsService {

    private final JdbcTemplate jdbcTemplate;

    public BuildingsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getBuildingsGeoJSON(Integer popId) {
        String sql = """
            WITH built_buildings AS (
                SELECT DISTINCT
                    UPPER(REGEXP_REPLACE(auftragsnummer, '_[^_]+$', '')) AS building_id
                FROM mastergrid
                WHERE civiel_einddatum IS NOT NULL
                  AND civiel_einddatum <> ''
            )
            SELECT jsonb_build_object(
                'type', 'FeatureCollection',
                'features', COALESCE(jsonb_agg(feature), '[]'::jsonb)
            )
            FROM (
                SELECT jsonb_build_object(
                    'type', 'Feature',
                    'geometry', ST_AsGeoJSON(b.geom)::jsonb,
                    'properties', to_jsonb(b) - 'geom' || jsonb_build_object(
                        'street_name', s.name,
                        'dp_name',     d.name,
                        'pop_name',    p.name,
                        'village_pop', b.village || '_' || p.name,
                        'total_homes', COUNT(DISTINCT h.home_id),
                        'homes_info',  COALESCE(hs.homes_info, '[]'::jsonb),
                        'built',       UPPER(b.building_id) = bb.building_id,
                        'building_id', UPPER(b.building_id)
                    )
                ) AS feature
                FROM buildings b
                JOIN pops p ON b.pop_id = p.pop_id
                LEFT JOIN streets s ON b.street_id = s.street_id
                LEFT JOIN dps d     ON s.dp_id = d.dp_id
                LEFT JOIN homes h   ON h.building_id = b.building_id
                LEFT JOIN built_buildings bb ON UPPER(b.building_id) = bb.building_id
                LEFT JOIN LATERAL (
                    SELECT jsonb_agg(
                        jsonb_build_object(
                            'home_id',         vs.home_id,
                            'anschlussstatus', vs.anschlussstatus_contracts,
                            'aufgabenstatus',  vs.aufgabenstatus,
                            'building_status', vs.building_status,
                            'has_class',       vs.has_class,
                            'phassive',        vs.pha,
                            'civil_einddatum', CASE
                                WHEN mg.civiel_einddatum IS NOT NULL AND mg.civiel_einddatum <> ''
                                THEN to_char(mg.civiel_einddatum::timestamp, 'YYYY-MM-DD')
                                ELSE 'Pending'
                            END
                        )
                    ) AS homes_info
                    FROM addresslist vs
                    LEFT JOIN mastergrid mg ON vs.home_id = mg.auftragsnummer
                    WHERE vs.building_id = UPPER(b.building_id)
                ) hs ON TRUE
                WHERE p.project_id = ?
                GROUP BY b.building_id, s.name, d.name, p.name, hs.homes_info, bb.building_id
            ) AS sub;
        """;

        return jdbcTemplate.queryForObject(
            sql,
            new Object[]{ popId },
            (rs, rowNum) -> Map.of("geojson", rs.getString(1))
        );
    }

}
