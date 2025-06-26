package com.appfibra.service;

import com.appfibra.DAO.ContractsDAO;
import com.appfibra.dto.DataTablesRequest;
import com.appfibra.dto.DataTablesRequest.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ContractsService {

    @Autowired
    private ContractsDAO contractsDAO;

    /**
     * Consulta paginada para la consulta original, basada en joins (contracts_list).
     */
    public Map<String, Object> getPaginatedContracts(Map<String, String> filters, int page, int size) {
        int offset = page * size;
        List<Map<String, Object>> data = contractsDAO.getContractsReport(filters, offset, size);
        int totalItems = contractsDAO.countContractsReport(filters);
        int totalPages = (int) Math.ceil((double) totalItems / size);
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("currentPage", page);
        result.put("totalItems", totalItems);
        result.put("totalPages", totalPages);
        return result;
    }

    /**
     * Procesa la solicitud de DataTables para la consulta original (contracts_list).
     */
    public Map<String, Object> handleDataTablesRequest(DataTablesRequest request) {
        Map<String, String> filters = new HashMap<>();
        if (request.getFilters() != null) {
            filters.putAll(request.getFilters());
        }
        if (request.getSearch() != null && request.getSearch().get("value") != null) {
            String globalSearch = request.getSearch().get("value").toString();
            if (!globalSearch.isEmpty()) {
                filters.put("globalSearch", globalSearch);
            }
        }
        if (request.getColumns() != null) {
            for (Column col : request.getColumns()) {
                String columnName = col.getData();
                String searchValue = col.getSearch() != null ? (String) col.getSearch().get("value") : null;
                if (searchValue != null && !searchValue.isEmpty()) {
                    filters.put(columnName, searchValue);
                }
            }
        }
        List<Map<String, Object>> data = contractsDAO.getContractsReport(filters, request.getStart(), request.getLength());
        int recordsFiltered = contractsDAO.countContractsReport(filters);
        int recordsTotal = contractsDAO.countContractsReport(new HashMap<>());
        Map<String, Object> result = new HashMap<>();
        result.put("draw", request.getDraw());
        result.put("recordsTotal", recordsTotal);
        result.put("recordsFiltered", recordsFiltered);
        result.put("data", data);
        return result;
    }

    /**
     * Obtiene métricas para el dashboard de manera dinámica.
     * Se requiere especificar la vista, el campo principal sobre el que agrupar y los filtros.
     */
    public Map<String, Object> getDashboardMetrics(String viewName, String field, Map<String, String> filters) {
        // Si filters no es nulo, elimina las claves que no corresponden a columnas
        if (filters != null) {
            filters.keySet().removeIf(key -> key.equalsIgnoreCase("view") || key.equalsIgnoreCase("source"));
        }
        return contractsDAO.getDashboardMetrics(viewName, field, filters);
    }

    /**
     * Obtiene datos de agregación, agrupados por dos campos específicos, con filtros.
     */
    public List<Map<String, Object>> getContractsAggregation(String field1, String field2, Map<String, String> filters) {
        return contractsDAO.getContractsAggregation(field1, field2, filters);
    }

    /**
     * Procesa la solicitud de DataTables para la vista extendida (por ejemplo, vw_buildings_status_extended).
     */
    public Map<String, Object> handleExtendedDataTablesRequest(DataTablesRequest request) {
        Map<String, String> filters = new HashMap<>();
        if (request.getFilters() != null) {
            filters.putAll(request.getFilters());
        }
        if (request.getColumns() != null) {
            for (Column col : request.getColumns()) {
                String value = col.getSearch() != null ? (String) col.getSearch().get("value") : null;
                if (value != null && !value.isEmpty()) {
                    filters.put(col.getData(), value);
                }
            }
        }
        List<Map<String, Object>> data = contractsDAO.getExtendedContractsReport(filters, request.getStart(), request.getLength());
        int recordsFiltered = contractsDAO.countExtendedContracts(filters);
        int recordsTotal = contractsDAO.countExtendedContracts(new HashMap<>());
        Map<String, Object> result = new HashMap<>();
        result.put("draw", request.getDraw());
        result.put("recordsTotal", recordsTotal);
        result.put("recordsFiltered", recordsFiltered);
        result.put("data", data);
        return result;
    }

    /**
     * Procesa la solicitud de DataTables para vistas dinámicas.
     * El objeto de filtros debe incluir la clave "view" para indicar qué vista usar.
     */
    public Map<String, Object> handleMultiViewDataTablesRequest(DataTablesRequest request) {
        // 1) Extraer los filtros base
        Map<String, String> filters = new HashMap<>();
        if (request.getFilters() != null) {
            filters.putAll(request.getFilters());
        }

        // 2) Leer el nombre de la vista ANTES de eliminar la clave "view"
        String viewName = "buildings_status_extended";  // valor por defecto
        if (request.getFilters() != null && request.getFilters().containsKey("view")) {
            viewName = request.getFilters().get("view");
        }

        // 3) Eliminar claves no deseadas del mapa de filtros
        filters.remove("view");
        filters.remove("source");

        // 4) Procesar las columnas del DataTablesRequest (que pueden añadir más filtros)
        if (request.getColumns() != null) {
            for (DataTablesRequest.Column col : request.getColumns()) {
                String value = (col.getSearch() != null ? (String) col.getSearch().get("value") : null);
                if (value != null && !value.isEmpty()) {
                    filters.put(col.getData(), value);
                }
            }
        }

        int offset = request.getStart();
        int limit = request.getLength();

        // 5) Llamar al DAO con la vista final y el mapa de filtros
        List<Map<String, Object>> data = contractsDAO.getDataFromView(viewName, filters, offset, limit);
        int recordsFiltered = contractsDAO.countDataFromView(viewName, filters);
        int recordsTotal = contractsDAO.countDataFromView(viewName, new HashMap<>());

        // 6) Respuesta final
        Map<String, Object> result = new HashMap<>();
        result.put("draw", request.getDraw());
        result.put("recordsTotal", recordsTotal);
        result.put("recordsFiltered", recordsFiltered);
        result.put("data", data);

        return result;
    }
    
    public List<Map<String, Object>> getHasAggregationByField(String groupField, Map<String, String> filters) {
        return contractsDAO.getHasAggregationByField(groupField, filters);
    }


}
