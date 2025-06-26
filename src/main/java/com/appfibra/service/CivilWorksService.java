package com.appfibra.service;

import com.appfibra.DAO.CivilWorksDAO;
import com.appfibra.DAO.QgisProjectDAO;
import com.appfibra.dto.DataTablesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CivilWorksService {

    private final CivilWorksDAO civilWorksDAO;
    private final QgisProjectDAO qgisProjectDAO;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CivilWorksService(CivilWorksDAO civilWorksDAO, QgisProjectDAO qgisProjectDAO, JdbcTemplate jdbcTemplate) {
        this.civilWorksDAO = civilWorksDAO;
        this.qgisProjectDAO = qgisProjectDAO;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Maneja la petición paginada para DataTables.
     */
    public Map<String, Object> handleDataTablesRequest(
            String source,
            int start,
            int length,
            String searchValue,
            List<DataTablesRequest.Column> columns,
            int draw,
            String client,
            Integer project_id
    ) {
        Map<String, String> filters = new HashMap<>();

        if (!searchValue.isEmpty()) {
            filters.put("globalSearch", searchValue);
        }
        if (client != null && !client.isEmpty()) {
            filters.put("client", client);
        }
        if (project_id != null && project_id > 0) {
            filters.put("project_id", String.valueOf(project_id));
        }

        if (columns != null) {
            for (DataTablesRequest.Column col : columns) {
                if (col.getSearch() != null && col.getSearch().get("value") != null) {
                    String colSearch = col.getSearch().get("value").toString();
                    if (!colSearch.isEmpty()) {
                        String colName = col.getData();
                        if ("pop_id".equals(colName)) colName = "pop";
                        if ("dp_id".equals(colName)) colName = "dp";
                        if ("street_id".equals(colName)) colName = "street";
                        filters.put(colName, colSearch);
                    }
                }
            }
        }

        int page = (length > 0) ? (start / length) : 0;

        Map<String, Object> result = getPaginatedData(source, filters, page, length);

        int totalRecords = civilWorksDAO.countTotalRecords(source);
        int filteredRecords = civilWorksDAO.countFilteredRecords(source, filters);
        double sumLengthFiltered = civilWorksDAO.getSumOfLengthFiltered(source, filters);
        double sumLengthTotal = civilWorksDAO.getSumOfLengthTotal(source);

        List<Map<String, Object>> dataList = (List<Map<String, Object>>) result.get("data");
        if (filteredRecords == 0 && dataList != null && !dataList.isEmpty()) {
            filteredRecords = totalRecords;
        }

        return Map.of(
            "draw", draw,
            "recordsTotal", totalRecords,
            "recordsFiltered", filteredRecords,
            "sumLengthFiltered", sumLengthFiltered,
            "sumLengthTotal", sumLengthTotal,
            "data", dataList
        );
    }

    public Map<String, Object> getPaginatedData(String source, Map<String, String> filters, int page, int size) {
        int offset = page * size;
        List<Map<String, Object>> data = civilWorksDAO.getPaginatedData(source, filters, offset, size);
        int totalItems = civilWorksDAO.countFilteredRecords(source, filters);

        return Map.of(
            "data", data,
            "currentPage", page,
            "totalItems", totalItems,
            "totalPages", (int) Math.ceil((double) totalItems / size)
        );
    }

    public List<Map<String, Object>> getFilteredData(String source, Map<String, String> filters) {
        return civilWorksDAO.getAllFilteredData(source, filters);
    }

    public List<Map<String, Object>> getCivilWorksByProject(int projectId) {
        return civilWorksDAO.getCivilWorksByProject(projectId);
    }

    public List<Map<String, Object>> getVillagesByProject(Long projectId) {
        String sql = """
            SELECT DISTINCT village AS village
            FROM civil_works
            WHERE project_id = ?
        """;
        return jdbcTemplate.queryForList(sql, projectId);
    }

    /**
     * Devuelve el GeoJSON de civil_works por pop_id, con filtros opcionales.
     */
    public Map<String, Object> getCivilWorksGeoJSON(Integer projectId, String type, String spec) {
        return qgisProjectDAO.getCivilWorksGeoJSONByProject(projectId, type, spec);
    }

    /**
     * Devuelve los valores únicos de 'type' por pop_id.
     */
    public List<String> getTypesByPop(Integer popId) {
        String sql = "SELECT DISTINCT type FROM civil_works WHERE pop_id = ? AND type IS NOT NULL ORDER BY type";
        return jdbcTemplate.queryForList(sql, String.class, popId);
    }

    /**
     * Devuelve los valores únicos de 'spec' por pop_id.
     */
    public List<String> getSpecsByPop(Integer popId) {
        String sql = "SELECT DISTINCT spec FROM civil_works WHERE pop_id = ? AND spec IS NOT NULL ORDER BY spec";
        return jdbcTemplate.queryForList(sql, String.class, popId);
    }
}
