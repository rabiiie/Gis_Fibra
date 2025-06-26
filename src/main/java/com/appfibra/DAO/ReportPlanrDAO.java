package com.appfibra.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ReportPlanrDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Devuelve la lista de pops distintos.
     */
    public List<String> findDistinctPops() {
        String sql = "SELECT DISTINCT pop FROM vw_report_planr ORDER BY pop";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    /**
     * Cuenta por combinación de delivery_status y anschlussstatus.
     * Devuelve maps con keys: delivery_status, anschlussstatus, count.
     */
    public List<Map<String, Object>> countByAnschlussstatus(String pop) {
        String sql = """
            SELECT
              CASE 
                WHEN fertigstellungsdatum_teilstueck IS NULL THEN 'Not Delivered'
                ELSE 'Delivered'
              END AS delivery_status,
              anschlussstatus,
              COUNT(*) AS count
            FROM vw_report_planr 
            WHERE pop = ?
              AND anschlussstatus IS NOT NULL
            GROUP BY
              CASE 
                WHEN fertigstellungsdatum_teilstueck IS NULL THEN 'Not Delivered'
                ELSE 'Delivered'
              END,
              anschlussstatus
            ORDER BY
              delivery_status,
              anschlussstatus
        """;
        return jdbcTemplate.queryForList(sql, pop);
    }

    /**
     * Cuenta por grundna.
     * Devuelve maps con keys: grundna, count.
     */
    public List<Map<String, Object>> countByGrundna(String pop) {
        String sql = """
            SELECT
              grundna,
              COUNT(*) AS count
            FROM vw_report_planr
            WHERE pop = ?
              AND grundna IS NOT NULL
            GROUP BY grundna
            ORDER BY grundna
        """;
        return jdbcTemplate.queryForList(sql, pop);
    }

    /**
     * Lista de DP para una combinación de delivery_status y anschlussstatus.
     */
    public List<String> findDpByStatusAndAnschluss(String pop, String deliveryStatus, String anschlussstatus) {
        String sql = """
            SELECT dp
            FROM vw_report_planr
            WHERE pop = ?
              AND CASE WHEN fertigstellungsdatum_teilstueck IS NULL THEN 'Not Delivered' ELSE 'Delivered' END = ?
              AND anschlussstatus = ?
            ORDER BY dp
        """;
        return jdbcTemplate.queryForList(sql, new Object[]{pop, deliveryStatus, anschlussstatus}, String.class);
    }

    /**
     * Lista de DP para un Grundna específico.
     */
    public List<String> findDpByGrundna(String pop, String grundna) {
        String sql = """
            SELECT dp
            FROM vw_report_planr
            WHERE pop = ?
              AND grundna = ?
            ORDER BY dp
        """;
        return jdbcTemplate.queryForList(sql, new Object[]{pop, grundna}, String.class);
    }

    /**
     * Cuenta cuántas filas hay por DP para un deliveryStatus + anschlussstatus dado.
     */
    public List<Map<String, Object>> countByDpAndStatus(String pop, String deliveryStatus, String anschlussstatus) {
        String sql = """
            SELECT dp, COUNT(*) AS count
            FROM vw_report_planr
            WHERE pop = ?
              AND (CASE WHEN fertigstellungsdatum_teilstueck IS NULL THEN 'Not Delivered' ELSE 'Delivered' END) = ?
              AND anschlussstatus = ?
            GROUP BY dp
            ORDER BY dp
        """;
        return jdbcTemplate.queryForList(sql, pop, deliveryStatus, anschlussstatus);
    }

    /**
     * Cuenta cuántas filas hay por DP para un grundna dado.
     */
    public List<Map<String, Object>> countByDpAndGrundna(String pop, String grundna) {
        String sql = """
            SELECT dp, COUNT(*) AS count
            FROM vw_report_planr
            WHERE pop = ?
              AND grundna = ?
            GROUP BY dp
            ORDER BY dp
        """;
        return jdbcTemplate.queryForList(sql, pop, grundna);
    }

}
