package com.appfibra.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class CivilWorksDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Devuelve registros paginados y filtrados (client, globalSearch, etc.)
     */
    public List<Map<String, Object>> getPaginatedData(
            String source,
            Map<String, String> filters,
            int offset,
            int limit
    ) {
        // Observa que en el SELECT devolvemos AS pop_id, dp_id, street_id
        StringBuilder sql = new StringBuilder("""
            SELECT
                p.name AS project,
                pop2.name AS pop,
                dp2.name  AS dp,
                s.name    AS street,
                s.tzip,
                cw.type,
                cw.spec,
                cw.length_meters,
                s.village
            FROM civil_works cw
            JOIN streets s       ON cw.street_id = s.street_id
            LEFT JOIN dps dp2    ON cw.dp_id = dp2.dp_id
            LEFT JOIN pops pop2  ON dp2.pop_id = pop2.pop_id
            LEFT JOIN projects p ON pop2.project_id = p.project_id
            LEFT JOIN clients c  ON p.client_id = c.client_id
            WHERE cw.source = ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(source);

        // Filtro por cliente
        if (filters.containsKey("client") && !filters.get("client").isEmpty()) {
            sql.append(" AND c.name ILIKE ?");
            params.add(filters.get("client"));
        }

        // Filtro global (ej. DataTables searchValue)
        if (filters.containsKey("globalSearch") && !filters.get("globalSearch").isEmpty()) {
            sql.append("""
                AND (
                    p.name ILIKE ?
                    OR pop2.name ILIKE ?
                    OR dp2.name  ILIKE ?
                    OR s.name    ILIKE ?
                    OR s.tzip    ILIKE ?
                    OR c.name    ILIKE ?
                    OR s.village ILIKE ?
                )
            """);
            String gs = filters.get("globalSearch");
            for (int i = 0; i < 7; i++) {
                params.add(gs);
            }
        }

        // Filtro por project_id
        if (filters.containsKey("project_id") && !filters.get("project_id").isEmpty()) {
            sql.append(" AND p.project_id = ? ");
            params.add(Integer.valueOf(filters.get("project_id")));
        }

        // Filtros individuales
        if (filters.containsKey("project") && !filters.get("project").isEmpty()) {
            sql.append(" AND p.name ILIKE ?");
            params.add(filters.get("project"));
        }
        if (filters.containsKey("pop") && !filters.get("pop").isEmpty()) {
            // pop2.name -> pop_id
            sql.append(" AND pop2.name ILIKE ?");
            params.add(filters.get("pop"));
        }
        if (filters.containsKey("dp") && !filters.get("dp").isEmpty()) {
            // dp2.name -> dp_id
            sql.append(" AND dp2.name ILIKE ?");
            params.add(filters.get("dp"));
        }
        if (filters.containsKey("street") && !filters.get("street").isEmpty()) {
            // s.name -> street_id
            sql.append(" AND s.name ILIKE ?");
            params.add(filters.get("street"));
        }
        if (filters.containsKey("tzip") && !filters.get("tzip").isEmpty()) {
            sql.append(" AND s.tzip ILIKE ?");
            params.add(filters.get("tzip"));
        }
        if (filters.containsKey("type") && !filters.get("type").isEmpty()) {
            sql.append(" AND cw.type ILIKE ?");
            params.add(filters.get("type"));
        }
        if (filters.containsKey("spec") && !filters.get("spec").isEmpty()) {
            sql.append(" AND cw.spec ILIKE ?");
            params.add(filters.get("spec"));
        }
        if (filters.containsKey("length_meters") && !filters.get("length_meters").isEmpty()) {
            sql.append(" AND CAST(cw.length_meters AS TEXT) ILIKE ?");
            params.add(filters.get("length_meters"));
        }
        if (filters.containsKey("village") && !filters.get("village").isEmpty()) {
            sql.append(" AND s.village ILIKE ?");
            params.add(filters.get("village"));
        }

        // Paginación
        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public List<Map<String, Object>> getCivilWorksByProject(int projectId) {
        String sql = """
            SELECT cw.civil_work_id, cw.type, cw.spec, cw.length_meters, s.name AS street, pop.name AS pop
            FROM civil_works cw
            JOIN streets s ON cw.street_id = s.street_id
            JOIN pops pop ON cw.pop_id = pop.pop_id
            WHERE pop.project_id = ?
        """;

        return jdbcTemplate.queryForList(sql, projectId);
    }

    public int countFilteredRecords(String source, Map<String, String> filters) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM civil_works cw
            JOIN streets s       ON cw.street_id = s.street_id
            LEFT JOIN dps dp2    ON cw.dp_id     = dp2.dp_id
            LEFT JOIN pops pop2  ON dp2.pop_id   = pop2.pop_id
            LEFT JOIN projects p ON pop2.project_id = p.project_id
            LEFT JOIN clients c  ON p.client_id     = c.client_id
            WHERE cw.source = ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(source);

        // Filtro por cliente
        if (filters.containsKey("client") && !filters.get("client").isEmpty()) {
            sql.append(" AND c.name ILIKE ?");
            params.add(filters.get("client"));
        }

        // Filtro global
        if (filters.containsKey("globalSearch") && !filters.get("globalSearch").isEmpty()) {
            sql.append("""
                AND (
                    p.name ILIKE ?
                    OR pop2.name ILIKE ?
                    OR dp2.name ILIKE ?
                    OR s.name ILIKE ?
                    OR s.tzip ILIKE ?
                    OR c.name ILIKE ?
                    OR s.village ILIKE ?
                )
            """);
            String gs = filters.get("globalSearch");
            params.add(gs);
            params.add(gs);
            params.add(gs);
            params.add(gs);
            params.add(gs);
            params.add(gs);
            params.add(gs);
        }

        // 🔴 Filtro por project_id
        if (filters.containsKey("project_id") && !filters.get("project_id").isEmpty()) {
            sql.append(" AND p.project_id = ? ");
            params.add(Integer.valueOf(filters.get("project_id")));
        }

        // Filtros individuales (igual que en getPaginatedData)
        if (filters.containsKey("project")) {
            sql.append(" AND p.name ILIKE ?");
            params.add(filters.get("project"));
        }
        if (filters.containsKey("pop")) {
            sql.append(" AND pop2.name ILIKE ?");
            params.add(filters.get("pop"));
        }
        if (filters.containsKey("dp")) {
            sql.append(" AND dp2.name ILIKE ?");
            params.add(filters.get("dp"));
        }
        if (filters.containsKey("street")) {
            sql.append(" AND s.name ILIKE ?");
            params.add(filters.get("street"));
        }
        if (filters.containsKey("tzip")) {
            sql.append(" AND s.tzip ILIKE ?");
            params.add(filters.get("tzip"));
        }
        if (filters.containsKey("type")) {
            sql.append(" AND cw.type ILIKE ?");
            params.add(filters.get("type"));
        }
        if (filters.containsKey("spec")) {
            sql.append(" AND cw.spec ILIKE ?");
            params.add(filters.get("spec"));
        }
        if (filters.containsKey("length_meters")) {
            sql.append(" AND CAST(cw.length_meters AS TEXT) ILIKE ?");
            params.add(filters.get("length_meters"));
        }
        if (filters.containsKey("village")) {
            sql.append(" AND s.village ILIKE ?");
            params.add(filters.get("village"));
        }

        return jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
    }
    
    public double getSumOfLengthFiltered(String source, Map<String, String> filters) {
        // Montar la misma query que countFilteredRecords, pero usando SUM en lugar de COUNT
        StringBuilder sql = new StringBuilder("""
            SELECT COALESCE(SUM(cw.length_meters), 0)
            FROM civil_works cw
            JOIN streets s       ON cw.street_id = s.street_id
            LEFT JOIN dps dp2    ON cw.dp_id     = dp2.dp_id
            LEFT JOIN pops pop2  ON dp2.pop_id   = pop2.pop_id
            LEFT JOIN projects p ON pop2.project_id = p.project_id
            LEFT JOIN clients c  ON p.client_id     = c.client_id
            WHERE cw.source = ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(source);

        // Repite los mismos filtros que en countFilteredRecords
        if (filters.containsKey("client") && !filters.get("client").isEmpty()) {
            sql.append(" AND c.name ILIKE ?");
            params.add(filters.get("client"));
        }
        if (filters.containsKey("globalSearch") && !filters.get("globalSearch").isEmpty()) {
            sql.append("""
                AND (
                    p.name ILIKE ?
                    OR pop2.name ILIKE ?
                    OR dp2.name ILIKE ?
                    OR s.name ILIKE ?
                    OR s.tzip ILIKE ?
                    OR c.name ILIKE ?
                    OR s.village ILIKE ?
                )
            """);
            String gs = filters.get("globalSearch");
            for (int i = 0; i < 7; i++) {
                params.add(gs);
            }
        }

        if (filters.containsKey("project_id") && !filters.get("project_id").isEmpty()) {
            sql.append(" AND p.project_id = ? ");
            params.add(Integer.valueOf(filters.get("project_id")));
        }
        if (filters.containsKey("project")) {
            sql.append(" AND p.name ILIKE ?");
            params.add(filters.get("project"));
        }
        if (filters.containsKey("pop")) {
            sql.append(" AND pop2.name ILIKE ?");
            params.add(filters.get("pop"));
        }
        if (filters.containsKey("dp")) {
            sql.append(" AND dp2.name ILIKE ?");
            params.add(filters.get("dp"));
        }
        if (filters.containsKey("street")) {
            sql.append(" AND s.name ILIKE ?");
            params.add(filters.get("street"));
        }
        if (filters.containsKey("tzip")) {
            sql.append(" AND s.tzip ILIKE ?");
            params.add(filters.get("tzip"));
        }
        if (filters.containsKey("type")) {
            sql.append(" AND cw.type ILIKE ?");
            params.add(filters.get("type"));
        }
        if (filters.containsKey("spec")) {
            sql.append(" AND cw.spec ILIKE ?");
            params.add(filters.get("spec"));
        }
        if (filters.containsKey("length_meters")) {
            sql.append(" AND CAST(cw.length_meters AS TEXT) ILIKE ?");
            params.add(filters.get("length_meters"));
        }
        if (filters.containsKey("village")) {
            sql.append(" AND s.village ILIKE ?");
            params.add(filters.get("village"));
        }

        Double result = jdbcTemplate.queryForObject(sql.toString(), Double.class, params.toArray());
        return (result != null) ? result : 0.0;
    }


    public int countTotalRecords(String source) {
        String sql = "SELECT COUNT(*) FROM civil_works WHERE source = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, source);
    }

    public List<Map<String, Object>> getAllFilteredData(
            String source,
            Map<String, String> filters
    ) {
        StringBuilder sql = new StringBuilder(
            "SELECT p.name AS project, " +
            "pop2.name AS pop, " +
            "dp2.name AS dp, " +
            "s.name AS street, " +
            "s.tzip, " +
            "cw.type, " +
            "cw.spec, " +
            "cw.length_meters, " +
            "s.village " +
            "FROM civil_works cw " +
            "JOIN streets s ON cw.street_id = s.street_id " +
            "LEFT JOIN dps dp2 ON cw.dp_id = dp2.dp_id " +
            "LEFT JOIN pops pop2 ON dp2.pop_id = pop2.pop_id " +
            "LEFT JOIN projects p ON pop2.project_id = p.project_id " +
            "LEFT JOIN clients c ON p.client_id = c.client_id " +
            "WHERE cw.source = ?"
        );

        List<Object> params = new ArrayList<>();
        params.add(source);

        if (filters.containsKey("client") && !filters.get("client").isEmpty()) {
            sql.append(" AND c.name ILIKE ?");
            params.add(filters.get("client"));
        }
        if (filters.containsKey("globalSearch") && !filters.get("globalSearch").isEmpty()) {
            sql.append(" AND (p.name ILIKE ? OR pop2.name ILIKE ? OR dp2.name ILIKE ? OR s.name ILIKE ? OR s.tzip ILIKE ? OR c.name ILIKE ? OR s.village ILIKE ?)");
            String gs = filters.get("globalSearch");
            params.add(gs);
            params.add(gs);
            params.add(gs);
            params.add(gs);
            params.add(gs);
            params.add(gs);
            params.add(gs);
        }

        // 🔴 Filtro por project_id en getAllFilteredData (si lo usas)
        if (filters.containsKey("project_id") && !filters.get("project_id").isEmpty()) {
            sql.append(" AND p.project_id = ? ");
            params.add(Integer.valueOf(filters.get("project_id")));
        }

        if (filters.containsKey("project") && !filters.get("project").isEmpty()) {
            sql.append(" AND p.name ILIKE ?");
            params.add(filters.get("project"));
        }
        if (filters.containsKey("pop") && !filters.get("pop").isEmpty()) {
            sql.append(" AND pop2.name ILIKE ?");
            params.add(filters.get("pop"));
        }
        if (filters.containsKey("dp") && !filters.get("dp").isEmpty()) {
            sql.append(" AND dp2.name ILIKE ?");
            params.add(filters.get("dp"));
        }
        if (filters.containsKey("street") && !filters.get("street").isEmpty()) {
            sql.append(" AND s.name ILIKE ?");
            params.add(filters.get("street"));
        }
        if (filters.containsKey("tzip") && !filters.get("tzip").isEmpty()) {
            sql.append(" AND s.tzip ILIKE ?");
            params.add(filters.get("tzip"));
        }
        if (filters.containsKey("type") && !filters.get("type").isEmpty()) {
            sql.append(" AND cw.type ILIKE ?");
            params.add(filters.get("type"));
        }
        if (filters.containsKey("spec") && !filters.get("spec").isEmpty()) {
            sql.append(" AND cw.spec ILIKE ?");
            params.add(filters.get("spec"));
        }
        if (filters.containsKey("length_meters") && !filters.get("length_meters").isEmpty()) {
            sql.append(" AND CAST(cw.length_meters AS TEXT) ILIKE ?");
            params.add(filters.get("length_meters"));
        }
        if (filters.containsKey("village") && !filters.get("village").isEmpty()) {
            sql.append(" AND s.village ILIKE ?");
            params.add(filters.get("village"));
        }

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public List<Map<String, Object>> getAllProjects() {
        String sql = """
            SELECT 
                p.project_id,
                p.engineering_pd, 
                p.construction_pd,
                p.country, 
                p.name, 
                p.phase, 
                p.subcontractor, 
                p.engineering_manager, 
                p.construction_manager,

                -- Suma de HPs por proyecto
                (SELECT COALESCE(SUM(a.hp), 0) 
                 FROM addresses a 
                 WHERE a.project_id = p.project_id) AS hp, 

                -- Cuenta clientes activos (status = 100)
                (SELECT COUNT(*) 
                 FROM addresses a 
                 WHERE a.project_id = p.project_id 
                   AND CAST(a.status AS INTEGER) = 100) AS activeclient, 

                -- Suma de civil works por proyecto
                (SELECT COALESCE(SUM(cw.length_meters), 0) 
                 FROM civil_works cw 
                 JOIN pops pop ON cw.pop_id = pop.pop_id
                 WHERE pop.project_id = p.project_id) AS civilworks

            FROM projects p;
        """;

        return jdbcTemplate.queryForList(sql);
    }

    public double getSumOfLengthTotal(String source) {
        String sql = """
            SELECT COALESCE(SUM(cw.length_meters), 0) 
            FROM civil_works cw
            WHERE cw.source = ?
        """;

        Double result = jdbcTemplate.queryForObject(sql, Double.class, source);
        return (result != null) ? result : 0.0;
    }

}
