package com.appfibra.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ClientDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Retorna todos los clientes (id + nombre)
     */
    public List<Map<String, Object>> getAllClients() {
        String sql = """
            WITH civil_works_totals AS (
                SELECT 
                    pop_id, 
                    COALESCE(SUM(length_meters), 0) AS total_civil_works
                FROM civil_works
                GROUP BY pop_id
            ),
            homes_totals AS (
                SELECT 
                    pr.client_id,
                    COUNT(h.home_id) AS total_hps,
                    COUNT(b.home_id) FILTER (
                        WHERE b.anschlussstatus_contracts = '100'
                    ) AS total_activations
                FROM homes h
                JOIN projects pr ON h.project_id = pr.project_id
                JOIN clients c ON pr.client_id = c.client_id
                LEFT JOIN vw_buildings_status_extended b
                  ON h.home_id = b.home_id
                GROUP BY pr.client_id
            )
            SELECT 
                c.client_id AS id, 
                c.name, 
                COALESCE(c.logo_url, '/static/default-logo.png') AS logoUrl, 
                COALESCE(SUM(cw.total_civil_works), 0) AS total_civil_works, 
                COALESCE(ht.total_hps, 0) AS total_hps, 
                COALESCE(ht.total_activations, 0) AS total_activations
            FROM clients c
            LEFT JOIN projects p ON c.client_id = p.client_id
            LEFT JOIN pops pop ON p.project_id = pop.project_id
            LEFT JOIN civil_works_totals cw ON pop.pop_id = cw.pop_id
            LEFT JOIN homes_totals ht ON c.client_id = ht.client_id
            GROUP BY c.client_id, c.name, c.logo_url, ht.total_hps, ht.total_activations
            ORDER BY c.name;
        """;

        return jdbcTemplate.queryForList(sql);
    }

    /**
     * Retorna proyectos de un cliente específico
     */
    public List<Map<String, Object>> getProjectsByClient(Long clientId) {
        String sql = """
            SELECT 
              p.project_id    AS id,
              p.name          AS name,
              p.country       AS country,
              p.phase         AS phase,
              p.subcontractor AS subcontractor
            FROM projects p
            WHERE p.client_id = ?
            ORDER BY p.name
        """;
        return jdbcTemplate.queryForList(sql, clientId);
    }

    public Map<String, Object> getClientById(Long clientId) {
        String sql = "SELECT * FROM clients WHERE client_id = ?";
        try {
            return jdbcTemplate.queryForMap(sql, clientId);
        } catch (EmptyResultDataAccessException e) {
            return null; // Si no hay resultados, devuelve null en lugar de lanzar un error
        }
    }

}
