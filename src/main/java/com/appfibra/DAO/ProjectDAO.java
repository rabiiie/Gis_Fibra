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

}
