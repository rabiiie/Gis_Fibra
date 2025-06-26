package com.appfibra.DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WochenmeldungDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Object[]> findProduccionAgrupadaPorPOP(Integer week1, Integer week2, Integer year) {
        String sql = """
            SELECT 
                projektname,
                teilpolygon,
                pop,
                COUNT(CASE WHEN semana = ?1 THEN 1 END) AS kw1_count,
                COUNT(CASE WHEN semana = ?2 THEN 1 END) AS kw2_count
            FROM vw_wochenmeldung
            WHERE ano = ?3
              AND CAST(anschlussstatus AS INTEGER) NOT IN (91, 99)
            GROUP BY projektname, teilpolygon, pop
            HAVING COUNT(CASE WHEN semana = ?1 THEN 1 END) > 0
                OR COUNT(CASE WHEN semana = ?2 THEN 1 END) > 0
            ORDER BY pop
        """;

        return entityManager.createNativeQuery(sql)
                .setParameter(1, week1)
                .setParameter(2, week2)
                .setParameter(3, year)
                .getResultList();
    }

    public List<Object[]> findProduccionAgrupadaPorDP(Integer week1, Integer week2, Integer year) {
        String sql = """
            SELECT 
                projektname,
                teilpolygon,
                pop,
                dp,
                COUNT(CASE WHEN semana = ?1 THEN 1 END) AS kw1_count,
                COUNT(CASE WHEN semana = ?2 THEN 1 END) AS kw2_count
            FROM vw_wochenmeldung
            WHERE ano = ?3
              AND CAST(anschlussstatus AS INTEGER) NOT IN (91, 99)
            GROUP BY projektname, teilpolygon, pop, dp
            HAVING COUNT(CASE WHEN semana = ?1 THEN 1 END) > 0
                OR COUNT(CASE WHEN semana = ?2 THEN 1 END) > 0
            ORDER BY pop, dp
        """;

        return entityManager.createNativeQuery(sql)
                .setParameter(1, week1)
                .setParameter(2, week2)
                .setParameter(3, year)
                .getResultList();
    }
}
