package com.appfibra.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class ViewComparisonDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> compareViews(String pop) {
        String sql = """
        SELECT 
            ar.home_id,
            COALESCE(ar.dp, pl.dp) as dp_ar,
            ar.reason_nc,
            ar.delivery_status,
            ar.cable_id,
            ar.odfcatv,
            ar.odfcatv_position,
            ar.odf,
            ar.odf_position,
            ar.delivery_date_passive,
            pl.auftragsnummer,
            pl.dp as dp_planr,
            pl.grundna,
            pl.anschlussstatus,
            pl.kabel_id_from_engineering,
            pl.odf_catv_from_engineering,
            pl.odf_catv_position_from_engineering,
            pl.odf_ip_from_engineering,
            pl.odf_ip_position_from_engineering,
            pl.fertigstellungsdatum_teilstueck,
            COALESCE(pl.pop, ar.area_pop) as pop
        FROM vw_ar_web ar
        FULL OUTER JOIN vw_report_planr pl ON ar.home_id = pl.auftragsnummer
        WHERE COALESCE(pl.pop, ar.area_pop) = ?
        AND (
            COALESCE(ar.dp, '') != COALESCE(pl.dp, '') OR
            COALESCE(ar.reason_nc, '') != COALESCE(pl.grundna, '') OR
            COALESCE(ar.delivery_status, '') != COALESCE(pl.anschlussstatus, '') OR
            COALESCE(ar.cable_id, '') != COALESCE(pl.kabel_id_from_engineering, '') OR
            COALESCE(ar.odfcatv, '') != COALESCE(pl.odf_catv_from_engineering, '') OR
            COALESCE(ar.odfcatv_position, '') != COALESCE(pl.odf_catv_position_from_engineering, '') OR
            COALESCE(ar.odf, '') != COALESCE(pl.odf_ip_from_engineering, '') OR
            COALESCE(ar.odf_position, '') != COALESCE(pl.odf_ip_position_from_engineering, '') OR
            TO_CHAR(ar.delivery_date_passive::date, 'YYYY-MM-DD') != TO_CHAR(pl.fertigstellungsdatum_teilstueck::date, 'YYYY-MM-DD')
        )
        """;
        return jdbcTemplate.queryForList(sql, pop);
    }

    public List<Map<String, Object>> getPopStatistics() {
        String sql = """
            SELECT 
                COALESCE(pl.pop, ar.area_pop) as pop,
                COUNT(*) as total_differences,
                SUM(CASE WHEN ar.dp != pl.dp THEN 1 ELSE 0 END) as dp_differences,
                SUM(CASE WHEN COALESCE(ar.reason_nc, '') != COALESCE(pl.grundna, '') THEN 1 ELSE 0 END) as reason_differences,
                SUM(CASE WHEN COALESCE(ar.delivery_status, '') != COALESCE(pl.anschlussstatus, '') THEN 1 ELSE 0 END) as status_differences,
                SUM(CASE WHEN COALESCE(ar.cable_id, '') != COALESCE(pl.kabel_id_from_engineering, '') THEN 1 ELSE 0 END) as cable_differences,
                SUM(CASE WHEN COALESCE(ar.odfcatv, '') != COALESCE(pl.odf_catv_from_engineering, '') THEN 1 ELSE 0 END) as odfcatv_differences,
                SUM(CASE WHEN COALESCE(ar.odfcatv_position, '') != COALESCE(pl.odf_catv_position_from_engineering, '') THEN 1 ELSE 0 END) as odfcatv_pos_differences,
                SUM(CASE WHEN COALESCE(ar.odf, '') != COALESCE(pl.odf_ip_from_engineering, '') THEN 1 ELSE 0 END) as odf_differences,
                SUM(CASE WHEN COALESCE(ar.odf_position, '') != COALESCE(pl.odf_ip_position_from_engineering, '') THEN 1 ELSE 0 END) as odf_pos_differences,
                SUM(CASE WHEN TO_CHAR(ar.delivery_date_passive::date, 'YYYY-MM-DD') != TO_CHAR(pl.fertigstellungsdatum_teilstueck::date, 'YYYY-MM-DD') THEN 1 ELSE 0 END) as delivery_date_differences
            FROM vw_ar_web ar
            FULL OUTER JOIN vw_report_planr pl ON ar.home_id = pl.auftragsnummer
            WHERE COALESCE(pl.pop, ar.area_pop) IS NOT NULL
            GROUP BY COALESCE(pl.pop, ar.area_pop)
            ORDER BY total_differences DESC
            """;
        return jdbcTemplate.queryForList(sql);
    }
}
