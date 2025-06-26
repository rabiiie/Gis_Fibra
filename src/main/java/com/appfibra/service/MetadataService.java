package com.appfibra.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetadataService {

    private final JdbcTemplate jdbcTemplate;

    public MetadataService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Obtiene la estructura de la base de datos y devuelve un esquema formateado.
     */
    public String getFormattedMetadata() {
        String query = """
            SELECT table_name, column_name, data_type, is_nullable, 
                   character_maximum_length, numeric_precision
            FROM information_schema.columns
            WHERE table_schema = 'public'
            ORDER BY table_name, ordinal_position
        """;

        List<TableColumn> columns = jdbcTemplate.query(query,
            (rs, rowNum) -> new TableColumn(
                rs.getString("table_name"),
                rs.getString("column_name"),
                rs.getString("data_type"),
                rs.getString("is_nullable"),
                rs.getObject("character_maximum_length") != null ? rs.getInt("character_maximum_length") : null,
                rs.getObject("numeric_precision") != null ? rs.getInt("numeric_precision") : null
            )
        );

        return columns.stream()
            .collect(Collectors.groupingBy(TableColumn::tableName))
            .entrySet()
            .stream()
            .map(entry -> formatTableSchema(entry.getKey(), entry.getValue()))
            .collect(Collectors.joining("\n\n"));
    }

    private String formatTableSchema(String tableName, List<TableColumn> columns) {
        String columnsStr = columns.stream()
            .map(col -> {
                StringBuilder sb = new StringBuilder();
                sb.append("- ").append(col.columnName())
                  .append(" (").append(col.dataType());
                if (col.charMaxLength() != null) {
                    sb.append(", max length: ").append(col.charMaxLength());
                }
                if (col.numericPrecision() != null) {
                    sb.append(", precision: ").append(col.numericPrecision());
                }
                sb.append(", ").append(col.isNullable().equalsIgnoreCase("YES") ? "NULLABLE" : "NOT NULL");
                sb.append(")");
                return sb.toString();
            })
            .collect(Collectors.joining("\n  "));

        return "Tabla: " + tableName + "\n  " + columnsStr;
    }

    private record TableColumn(
            String tableName,
            String columnName,
            String dataType,
            String isNullable,
            Integer charMaxLength,
            Integer numericPrecision
    ) {}
}
