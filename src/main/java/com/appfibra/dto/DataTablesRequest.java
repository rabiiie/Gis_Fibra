package com.appfibra.dto;

import java.util.List;
import java.util.Map;

public class DataTablesRequest {
    private int draw;
    private int start;
    private int length;
    private Map<String, Object> search; // { "value": "...", "regex": false }
    private List<Column> columns;
    private List<Order> order;
    
    private String client;      // Filtro de cliente
    private Integer project_id; // Filtro de projectId
    
    // NUEVO: Propiedad para filtros extra (por ejemplo, de inputs de la tabla)
    private Map<String, String> filters;

    // GETTERS / SETTERS
    public int getDraw() { return draw; }
    public void setDraw(int draw) { this.draw = draw; }

    public int getStart() { return start; }
    public void setStart(int start) { this.start = start; }

    public int getLength() { return length; }
    public void setLength(int length) { this.length = length; }

    public Map<String, Object> getSearch() { return search; }
    public void setSearch(Map<String, Object> search) { this.search = search; }

    public List<Column> getColumns() { return columns; }
    public void setColumns(List<Column> columns) { this.columns = columns; }

    public List<Order> getOrder() { return order; }
    public void setOrder(List<Order> order) { this.order = order; }

    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }

    public Integer getProject_id() { return project_id; }
    public void setProject_id(Integer project_id) { this.project_id = project_id; }

    public Map<String, String> getFilters() { return filters; }
    public void setFilters(Map<String, String> filters) { this.filters = filters; }

    // Clases internas:
    public static class Column {
        private String data;
        private String name;
        private boolean searchable;
        private boolean orderable;
        private Map<String, Object> search;

        public Column() {}

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public boolean isSearchable() { return searchable; }
        public void setSearchable(boolean searchable) { this.searchable = searchable; }

        public boolean isOrderable() { return orderable; }
        public void setOrderable(boolean orderable) { this.orderable = orderable; }

        public Map<String, Object> getSearch() { return search; }
        public void setSearch(Map<String, Object> search) { this.search = search; }
    }

    public static class Order {
        private int column;
        private String dir;
        public Order() {}

        public int getColumn() { return column; }
        public void setColumn(int column) { this.column = column; }

        public String getDir() { return dir; }
        public void setDir(String dir) { this.dir = dir; }
    }
}
