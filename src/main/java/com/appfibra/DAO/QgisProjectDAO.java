package com.appfibra.DAO;

import com.appfibra.model.QgisProject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class QgisProjectDAO {

    private final JdbcTemplate jdbcTemplate;

    public QgisProjectDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper para convertir ResultSet en QgisProject
    private final RowMapper<QgisProject> projectRowMapper = (rs, rowNum) -> new QgisProject(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("description"),
        rs.getDouble("lat"),
        rs.getDouble("lon")
    );

    // Obtener todos los proyectos
    public List<QgisProject> findAll() {
        String sql = "SELECT * FROM qgis_projects";
        return jdbcTemplate.query(sql, projectRowMapper);
    }

    // Buscar un proyecto por ID
    public QgisProject findById(Long id) {
        String sql = "SELECT * FROM qgis_projects WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, projectRowMapper, id);
    }

    // Insertar un nuevo proyecto
    public int insert(QgisProject project) {
        String sql = "INSERT INTO qgis_projects (name, description, lat, lon) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, project.getName(), project.getDescription(), project.getLat(), project.getLon());
    }

    // Actualizar un proyecto
    public int update(QgisProject project) {
        String sql = "UPDATE qgis_projects SET name = ?, description = ?, lat = ?, lon = ? WHERE id = ?";
        return jdbcTemplate.update(sql, project.getName(), project.getDescription(), project.getLat(), project.getLon(), project.getId());
    }

    // Eliminar un proyecto
    public int delete(Long id) {
        String sql = "DELETE FROM qgis_projects WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }


    public Map<String, Object> getCatasterGeoJSON(String tableName) {
        String sql = String.format("""
            SELECT jsonb_build_object(
                'type', 'FeatureCollection',
                'features', COALESCE(jsonb_agg(
                    jsonb_build_object(
                        'type', 'Feature',
                        'geometry', ST_AsGeoJSON(ST_Transform(geom, 4326))::jsonb,
                        'properties', to_jsonb(t) - 'geom'
                    )
                ), '[]'::jsonb)
            ) AS geojson
            FROM dgf."%s" t
        """, tableName);

        try {
            return jdbcTemplate.queryForObject(
                sql, (rs, rowNum) -> Map.of("geojson", rs.getString("geojson"))
            );
        } catch (Exception e) {
            System.err.println("❌ Error al obtener catastro para " + tableName + ": " + e.getMessage());
            return Map.of("geojson", "{}");
        }
    }

    public Map<String, Object> getBuildingsGeoJSON(String village) {
        String tableName = village + "_buildings";
        String sql = String.format("""
            SELECT jsonb_build_object(
                'type', 'FeatureCollection',
                'features', COALESCE(jsonb_agg(
                    jsonb_build_object(
                        'type', 'Feature',
                        'geometry', ST_AsGeoJSON(ST_Transform(geom, 4326))::jsonb,
                        'properties', to_jsonb(t) - 'geom'
                    )
                ), '[]'::jsonb)
            ) AS geojson
            FROM dgf."%s" t
        """, tableName);

        try {
            String geojsonString = jdbcTemplate.queryForObject(sql, String.class);
            ObjectMapper mapper = new ObjectMapper();
            return Map.of("geojson", mapper.readValue(geojsonString, Map.class));
        } catch (Exception e) {
            System.err.println("❌ Error al obtener buildings: " + e.getMessage());
            return Map.of("geojson", Map.of("type", "FeatureCollection", "features", List.of()));
        }
    }

	
	public Map<String, Object> getProjectByName(String projectName) {
	    String sql = "SELECT * FROM qgis_projects WHERE name = ?";
	    return jdbcTemplate.queryForMap(sql, projectName);
	}


	public Map<String, Object> getProjectLayersGeoJSON(String village) {
	    String sql = String.format("""
	        SELECT jsonb_build_object(
	            'type', 'FeatureCollection',
	            'features', jsonb_agg(
	                jsonb_build_object(
	                    'type', 'Feature',
	                    'geometry', ST_AsGeoJSON(geom)::jsonb,
	                    'properties', jsonb_build_object(
	                        'id', id,
	                        'type', type,
	                        'length_m', "length (m)",
	                        'dp', dp,
	                        'depth', depth,
	                        'cover', cover
	                    )
	                )
	            )
	        ) AS geojson
	        FROM dgf."%s"
	    """, village); // 🔹 Se construye dinámicamente con el nombre del village

	    try {
	        return jdbcTemplate.queryForObject(
	            sql,
	            (rs, rowNum) -> Map.of("geojson", rs.getString("geojson"))
	        );
	    } catch (Exception e) {
	        System.err.println("❌ Error al obtener las capas del proyecto para " + village + ": " + e.getMessage());
	        return Map.of("geojson", "{}"); // 🔹 Devuelve un JSON vacío si hay error
	    }
	}

	public Map<String, Object> getCivilWorksGeoJSONByProject(Integer projectId, String type, String spec) {
	    StringBuilder sql = new StringBuilder("""
	        WITH built_labels AS (
    -- 1. Construidos por building_id (GardenTrench y BuildingCross)
    SELECT DISTINCT UPPER(building_id) AS label
    FROM mastergrid
    WHERE civiel_einddatum IS NOT NULL

    UNION

    -- 2. Construidos por label_dduct (Trench, Drilling tipo Ducto)
    SELECT DISTINCT UPPER(h.label_dduct) AS label
    FROM homes h
    JOIN mastergrid m ON UPPER(h.home_id) = UPPER(m.auftragsnummer)
    WHERE m.civiel_einddatum IS NOT NULL
)

SELECT jsonb_build_object(
    'type', 'FeatureCollection',
    'features', COALESCE(jsonb_agg(
        jsonb_build_object(
            'type', 'Feature',
            'geometry', ST_AsGeoJSON(c.geom)::jsonb,
            'properties', jsonb_build_object(
                'civil_work_id', c.civil_work_id,
                'dp_id',         c.dp_id,
                'dp_name',       d.name,
                'pop_id',        c.pop_id,
                'pop_name',      p.name,
                'street_id',     c.street_id,
                'street_name',   s.name,
                'type',          c.type,
                'spec',          c.spec,
                'status',        c.status,
                'layer',         c.layer,
                'length_m',      c.length_meters,
                'village',       c.village,
                'village_pop',   c.village || '_' || p.name,
                'tzip',          c.tzip,
                'geom_hash',     c.geom_hash,
                'parent_id',     c.parent_id,
                'label',         c.label,
                'built',         (bl.label IS NOT NULL)
            )
        )
    ), '[]'::jsonb)
) AS geojson
FROM civil_works c
JOIN pops p     ON c.pop_id = p.pop_id
LEFT JOIN dps d ON c.dp_id = d.dp_id
LEFT JOIN streets s ON c.street_id = s.street_id
LEFT JOIN built_labels bl ON UPPER(c.label) = bl.label
WHERE p.project_id = ?


	    """);

	    List<Object> params = new ArrayList<>();
	    params.add(projectId);

	    if (type != null && !type.isBlank()) {
	        sql.append(" AND c.type = ? ");
	        params.add(type);
	    }

	    if (spec != null && !spec.isBlank()) {
	        sql.append(" AND c.spec = ? ");
	        params.add(spec);
	    }

	    return jdbcTemplate.queryForObject(
	        sql.toString(),
	        (rs, rowNum) -> Map.of("geojson", rs.getString("geojson")),
	        params.toArray()
	    );
	}


}
