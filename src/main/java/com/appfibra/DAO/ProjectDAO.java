package com.appfibra.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ProjectDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Devuelve TODOS los proyectos para tu tabla principal de "Projects".
     */
    public List<Map<String, Object>> getAllProjects() {
        String sql = """
            SELECT
                p.project_id            AS project_id,
                p.engineering_pd        AS engineering_pd,
                p.construction_pd       AS construction_pd,
                p.country               AS country,
                p.name                  AS name,
                p.phase                 AS phase,
                p.subcontractor         AS subcontractor,
                p.engineering_manager   AS engineering_manager,
                p.construction_manager  AS construction_manager,
                p.lat                   AS lat,
                p.lng                   AS lng,

                (SELECT COALESCE(SUM(a.hp), 0) 
                 FROM addresses a 
                 WHERE a.project_id = p.project_id) AS hp,

                (SELECT COALESCE(SUM(a.activations), 0)
                 FROM addresses a
                 WHERE a.project_id = p.project_id) AS total_activations,

                (SELECT COALESCE(SUM(cw.length_meters), 0) 
                 FROM civil_works cw 
                 JOIN pops pop ON cw.pop_id = pop.pop_id
                 WHERE pop.project_id = p.project_id) AS civilworks,

                (SELECT COALESCE(SUM(a.contracts), 0) 
                 FROM addresses a 
                 WHERE a.project_id = p.project_id) AS total_contracts

            FROM projects p
            ORDER BY p.project_id;
        """;
        return jdbcTemplate.queryForList(sql);
    }


    /**
     * Devuelve detalles de un proyecto (opcional, si quieres mostrar detalles extras).
     */
    public Map<String, Object> getProjectById(int projectId) {
        String sql = """
            SELECT
                p.project_id            AS project_id,
                p.name                  AS name,
                p.country               AS country,
                p.phase                 AS phase,
                p.lat                   AS lat,
                p.lng                   AS lng,

                (SELECT COALESCE(SUM(a.hp), 0) 
                 FROM addresses a 
                 WHERE a.project_id = p.project_id) AS hp,

                (SELECT COALESCE(SUM(a.activations), 0)
                 FROM addresses a
                 WHERE a.project_id = p.project_id) AS total_activations,

                (SELECT COALESCE(SUM(cw.length_meters), 0) 
                 FROM civil_works cw 
                 JOIN pops pop ON cw.pop_id = pop.pop_id
                 WHERE pop.project_id = p.project_id) AS civilworks

            FROM projects p
            WHERE p.project_id = ?;
        """;

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, projectId);
        return result.isEmpty() ? null : result.get(0);
    }
    public List<Map<String, Object>> getProjectsByClientId(int clientId) {
        String sql = """
            SELECT
                p.project_id,
                p.name,
                p.country,
                p.phase,
                p.subcontractor,
                p.lat,
                p.lng,

                -- Total metros civil_works
                COALESCE((
                    SELECT SUM(cw.length_meters)
                    FROM civil_works cw
                    JOIN pops pop ON cw.pop_id = pop.pop_id
                    WHERE pop.project_id = p.project_id
                ), 0) AS total_civil_works,

                -- Total hogares (homes)
                COALESCE((
                    SELECT COUNT(*)
                    FROM homes h
                    WHERE h.project_id = p.project_id
                ), 0) AS total_hps,

                -- Total contratos activos
                COALESCE((
                    SELECT COUNT(*)
                    FROM contracts_list cl
                    WHERE cl.project_code = p.project_id::text
                ), 0) AS total_activations

            FROM projects p
            WHERE p.client_id = ?
            ORDER BY p.project_id;
        """;

        return jdbcTemplate.queryForList(sql, clientId);
    }
    public List<Map<String, Object>> getProjectsWithoutCoordinates() {
        String sql = "SELECT project_id, name FROM projects WHERE lat IS NULL OR lng IS NULL";
        return jdbcTemplate.queryForList(sql);
    }

    public int updateCoordinatesById(Long id, double lat, double lng) {
        String sql = "UPDATE projects SET lat = ?, lng = ? WHERE project_id = ?";
        return jdbcTemplate.update(sql, lat, lng, id);
    }





}
