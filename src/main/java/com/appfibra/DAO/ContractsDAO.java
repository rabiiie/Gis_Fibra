package com.appfibra.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.appfibra.utils.SqlFilterBuilder;
import com.appfibra.utils.ViewColumnMappings;

import java.util.*;

@Repository
public class ContractsDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ---------------------------------------------------------
    // 1. Métodos tradicionales (joins en tablas)
    // ---------------------------------------------------------
    public List<Map<String, Object>> getContractsReport(Map<String, String> filters, int offset, int limit) {
        String baseSql = """
            SELECT
			    c.home_id,
			    c.subtype,
			    c.access_location,
			    c.AufgabenStatus,
			    c.building_id,
			    b.status_1 AS building_status,
			    b.status_updated_at AS building_status_updated_at,
			    b.action_holder AS building_action_holder,
			    b.status_hausbegehung_hvs_from_house AS building_hausbegehung_status,
			    u.assignment_number AS unit_id,
			    u.status_1 AS unit_status,
			    u.status_updated_at AS unit_status_updated_at,
			    u.action_holder AS unit_action_holder,
			    u.status_hausbegehung AS unit_hausbegehung_status,
			    m.anschlussstatus,
			    m.tiefbaudatum,
			    m.activeoperator_name_from_tri,
			    o.outsource_name,
			    bl.order_date,
			    FLOOR((CURRENT_DATE - TO_DATE(c.order_date, 'DD.MM.YYYY')) / 7) AS contract_age_weeks
			FROM contracts_list c
			LEFT JOIN task_buildings b ON c.building_id = b.assignment_number
			LEFT JOIN (
			  SELECT DISTINCT ON (home_id) *
			  FROM baulist_highlights
			  ORDER BY home_id, order_date DESC NULLS LAST
			) bl ON c.home_id = bl.home_id



			LEFT JOIN task_units u     ON c.home_id     = u.assignment_number
			LEFT JOIN mastergrid m     ON m.Auftragsnummer = u.assignment_number
			LEFT JOIN outsources o     ON LOWER(c.access_location) = LOWER(o.AP)
        """;

        SqlFilterBuilder builder = new SqlFilterBuilder();
        builder.addTextFilter("c.home_id",                      filters.get("home_id"));
        builder.addTextFilter("c.subtype",                      filters.get("subtype"));
        builder.addTextFilter("c.access_location",              filters.get("access_location"));
        builder.addTextFilter("c.AufgabenStatus",               filters.get("aufgabenstatus"));
        builder.addTextFilter("c.building_id",                  filters.get("building_id"));
        builder.addTextFilter("b.status_1",                     filters.get("building_status"));
        builder.addTextFilter("b.status_updated_at",            filters.get("building_status_updated_at"));
        builder.addTextFilter("b.action_holder",                filters.get("building_action_holder"));
        builder.addTextFilter("b.status_hausbegehung_hvs_from_house", filters.get("building_hausbegehung_status"));
        builder.addTextFilter("u.assignment_number",            filters.get("unit_id"));
        builder.addTextFilter("u.status_1",                     filters.get("unit_status"));
        builder.addTextFilter("u.status_updated_at",            filters.get("unit_status_updated_at"));
        builder.addTextFilter("u.action_holder",                filters.get("unit_action_holder"));
        builder.addTextFilter("u.status_hausbegehung",          filters.get("unit_hausbegehung_status"));
        builder.addTextFilter("m.anschlussstatus",              filters.get("anschlussstatus"));
        builder.addTextFilter("m.tiefbaudatum",                 filters.get("tiefbaudatum"));
        builder.addTextFilter("m.activeoperator_name_from_tri", filters.get("activeoperator_name_from_tri"));
        builder.addTextFilter("o.outsource_name",               filters.get("outsource_name"));
        builder.addTextFilter("c.order_date",                   filters.get("order_date"));


        StringBuilder sql = new StringBuilder(baseSql)
            .append(builder.buildWhereClause())
            .append("""
                ORDER BY
                    c.subtype, c.access_location, c.AufgabenStatus,
                    b.assignment_number, u.assignment_number
            """);
        if (limit > 0) {
            sql.append(" LIMIT ? OFFSET ?");
        }

        List<Object> params = new ArrayList<>(builder.getParams());
        if (limit > 0) {
            params.add(limit);
            params.add(offset);
        }

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public int countContractsReport(Map<String, String> filters) {
        String baseSql = """
            SELECT COUNT(*)
            FROM task_buildings b
            LEFT JOIN task_units u     ON b.assignment_number = u.building_id
            LEFT JOIN contracts_list c ON c.building_id      = b.assignment_number
            LEFT JOIN mastergrid m     ON m.Auftragsnummer    = u.assignment_number
            LEFT JOIN outsources o     ON LOWER(c.access_location) = LOWER(o.AP)
        """;

        SqlFilterBuilder builder = new SqlFilterBuilder();
        builder.addTextFilter("c.subtype",         filters.get("subtype"));
        builder.addTextFilter("c.access_location", filters.get("access_location"));
        builder.addTextFilter("b.status_1",        filters.get("building_status"));
        builder.addTextFilter("c.AufgabenStatus",  filters.get("aufgabenstatus"));

        String sql = baseSql + builder.buildWhereClause();
        return jdbcTemplate.queryForObject(sql, Integer.class, builder.getParams().toArray());
    }

    // ---------------------------------------------------------
    // 2. Validación dinámica de columnas para vistas simples
    // ---------------------------------------------------------
    public Set<String> getValidColumns(String viewName) {
        String realTable = switch (viewName.toLowerCase()) {
            case "buildings_status_extended" -> "vw_buildings_status_extended";
            case "planner"                   -> "mastergrid";
            case "task_units"                -> "task_units";
            case "buildings_task"            -> "vw_buildings_task";
            case "baulist_highlights"        -> "baulist_highlights";
            case "activations_highlights"    -> "activations_highlights";
            case "ar_web"			   -> "ar_web";
            default                           -> viewName.toLowerCase();
        };
        String sql = """
            SELECT lower(column_name)
            FROM information_schema.columns
            WHERE table_name = ?
        """;
        return new HashSet<>(jdbcTemplate.queryForList(sql, String.class, realTable));
    }

    public String getColumnForField(String viewName, String field) {
        String fld = field.toLowerCase();
        if (!getValidColumns(viewName).contains(fld)) {
            throw new IllegalArgumentException("Campo no válido: " + field);
        }
        return field;
    }
    // ---------------------------------------------------------
    // 3. Vistas dinámicas: getDataFromView, countDataFromView
    // ---------------------------------------------------------
    private static final Set<String> numericFields = Set.of(
        "has0_count", "has1_count", "has2_count",
        "total_units", "planner_contracts", "pom_contracts","year","order_year", "order_week", "change_year", "change_week"
    );
    
    /*-------------------------------------------------
    Alias “campo del frontend”  →  “columna real SQL”
   -------------------------------------------------*/
    /* =========================================================================
    Alias  (nombre que llega del frontend)  →  (columna real en la vista)
    ========================================================================= */
    private static final Map<String, String> FIELD_ALIASES = Map.ofEntries(
    	    // mastergrid
    	    Map.entry("civiel startdatum", "civiel_startdatum"),
    	    Map.entry("civiel einddatum",  "civiel_einddatum"),
    	    Map.entry("tiefbaudatum",      "tiefbaudatum"),

    	    // ar_web (nombres como en el Excel o frontend)
    	    Map.entry("date last update", "date_last_update"),
    	    Map.entry("tijdstip laatste wijziging", "date_last_update"),
    	    Map.entry("start date civil work", "start_date_civil_work"),
    	    Map.entry("civieldatum", "start_date_civil_work"),
    	    Map.entry("plan date has", "plan_date_has"),
    	    Map.entry("plandatum", "plan_date_has"),
    	    Map.entry("has date", "has_date"),
    	    Map.entry("hasdatum", "has_date"),
    	    Map.entry("hp plandate", "hp_plandate"),
    	    Map.entry("delivery date passive", "delivery_date_passive"),
    	    Map.entry("opleverdatum", "delivery_date_passive")
    	);

 private static final Set<String> TEXT_DATE_FIELDS = Set.of(
		    // mastergrid
		    "plandatum", "tiefbaudatum",
		    "civiel_startdatum", "civiel_einddatum",
		    "order_date",

		    // ar_web
		    "date_last_update",
		    "start_date_civil_work",
		    "plan_date_has",
		    "has_date",
		    "hp_plandate",
		    "delivery_date_passive"
		);



 /* =========================================================================
    1.  getDataFromView
    ========================================================================= */
 public List<Map<String,Object>> getDataFromView(
         String viewName, Map<String,String> filters, int offset, int limit) {

     // Caso especial contracts_list
     if ("contracts_list".equalsIgnoreCase(viewName)) {
         return getContractsReport(filters, offset, limit);
     }

     // Elegir tabla y orden
     String baseSql, orderCol;
     switch (viewName.toLowerCase()) {
         case "planner"                   -> { baseSql = "SELECT * FROM mastergrid";               orderCol = "dp_name"; }
         case "task_units"                -> { baseSql = "SELECT * FROM task_units";             orderCol = "assignment_number"; }
         case "buildings_task"            -> { baseSql = "SELECT * FROM task_buildings";        orderCol = "building_id"; }
         case "baulist_highlights"        -> { baseSql = "SELECT * FROM baulist_highlights";    orderCol = "home_id, building_id"; }
         case "activations_highlights"    -> { baseSql = "SELECT * FROM activations_highlights";orderCol = "bestellnummer"; }
         case "buildings_status_extended" -> { baseSql = "SELECT * FROM vw_buildings_status_extended"; orderCol = "home_id, building_id"; }
         case "ar_web"                   -> { baseSql = "SELECT * FROM ar_web";                orderCol = "home_id"; }
         default -> throw new IllegalArgumentException("Vista no válida: " + viewName);
     }

     SqlFilterBuilder builder = new SqlFilterBuilder();
     List<String> excluded = List.of("view","source");

     for (var e : filters.entrySet()) {
    	// justo al empezar el for de filtros:
    	 String fld = e.getKey();           // nombre que viene del UI ("civiel startdatum")
    	 String raw = e.getValue();
    	 if (raw == null || raw.isBlank() || excluded.contains(fld.toLowerCase())) continue;

    	 // **resolvemos alias frontend → columna real**
    	 String dbField = FIELD_ALIASES.getOrDefault(fld.toLowerCase(), fld);
    	 String col;
    	 try {
    	     col = getColumnForField(viewName, dbField);
    	     System.out.println("➡️ Campo filtrado: '" + fld + "' → columna real: '" + col + "' con valor: " + raw);
    	 } catch (IllegalArgumentException ex) {
    	     continue;
    	 }


         // 1) numérico
    	 if (numericFields.contains(col)) {
    		    builder.addNumericFilter(col, raw);

    		} else if (raw.toUpperCase().startsWith("W") || raw.matches("\\d{4}-W.*")) {
    		    // columnas TEXT con fecha
    			boolean isTextDate = TEXT_DATE_FIELDS.contains(col.toLowerCase(Locale.ROOT));

    		    builder.addWeekRangeFilter(col, raw, isTextDate);

    		} else {
    		    builder.addTextFilter(col, raw);
    		}

     }
     // 4) construir SQL
     System.out.println("🏷 Filters recibidos para " + viewName + ": " + filters);


     StringBuilder sql = new StringBuilder(baseSql)
         .append(builder.buildWhereClause())
         .append(" ORDER BY ").append(orderCol);
     if (limit > 0) sql.append(" LIMIT ? OFFSET ?");

     List<Object> params = new ArrayList<>(builder.getParams());
     if (limit > 0) { params.add(limit); params.add(offset); }
     System.out.println("🏷 SQL generado: " + sql);
     System.out.println("🏷 Parámetros: " + params);

     return jdbcTemplate.queryForList(sql.toString(), params.toArray());
 }

 /* =========================================================================
    2.  countDataFromView
    ========================================================================= */
 public int countDataFromView(String viewName, Map<String,String> filters) {
     if ("contracts_list".equalsIgnoreCase(viewName)) {
         return countContractsReport(filters);
     }

     String countSql = switch (viewName.toLowerCase()) {
         case "planner"                   -> "SELECT COUNT(*) FROM mastergrid";
         case "task_units"                -> "SELECT COUNT(*) FROM task_units";
         case "buildings_task"            -> "SELECT COUNT(*) FROM task_buildings";
         case "buildings_status_extended" -> "SELECT COUNT(*) FROM vw_buildings_status_extended";
         case "baulist_highlights"        -> "SELECT COUNT(*) FROM baulist_highlights";
         case "activations_highlights"    -> "SELECT COUNT(*) FROM activations_highlights";
         case "ar_web"                   -> "SELECT COUNT(*) FROM ar_web";
         default -> throw new IllegalArgumentException("Vista no válida: " + viewName);
     };

     SqlFilterBuilder builder = new SqlFilterBuilder();
     for (var e : filters.entrySet()) {
         String fld = e.getKey();
         String raw = e.getValue();
         if (raw == null || raw.isBlank()
             || fld.equalsIgnoreCase("view")
             || fld.equalsIgnoreCase("source")) continue;

         String dbField = FIELD_ALIASES.getOrDefault(fld.toLowerCase(), fld);
         String col;
         try { col = getColumnForField(viewName, dbField); }
         catch (IllegalArgumentException ex) { continue; }

         if (numericFields.contains(col)) {
             builder.addNumericFilter(col, raw);
         } else if (raw.toUpperCase().startsWith("W") || raw.matches("\\d{4}-W.*")) {
        	 boolean isTextDate = TEXT_DATE_FIELDS.contains(col.toLowerCase(Locale.ROOT));

             builder.addWeekRangeFilter(col, raw, isTextDate);
         } else {
             builder.addTextFilter(col, raw);
         }
     }

     String sql = countSql + builder.buildWhereClause();
     return jdbcTemplate.queryForObject(sql, Integer.class, builder.getParams().toArray());
 }


    // ---------------------------------------------------------
    // 4. Dashboard Metrics
    // ---------------------------------------------------------
	 public Map<String,Object> getDashboardMetrics(String viewName,
	         String field,
	         Map<String,String> filters) {
		Map<String,Object> result = new LinkedHashMap<>();
		
		// si es listado de contratos, delegar
		if ("contracts_list".equalsIgnoreCase(viewName) && !field.equalsIgnoreCase("anschlussstatus_contracts")) {
		// no aplicable aquí
		return Map.of();
		}
		
		// preparativos idénticos a getDataFromView
		String column;
		if (field.equalsIgnoreCase("anschlussstatus_contracts")) {
		column = "anschlussstatus_contracts";
		} else {
		column = getColumnForField(viewName, field);
		}
		String realView = "vw_buildings_status_extended";
		
		StringBuilder sql = new StringBuilder(field.equalsIgnoreCase("anschlussstatus_contracts")
		? """
		SELECT TRIM(unnest(string_to_array(anschlussstatus_contracts, ','))) AS key,
		COUNT(*) AS value
		FROM vw_buildings_status_extended
		"""
		: "SELECT " + column + " AS key, COUNT(*) AS value FROM " + realView);
		
		SqlFilterBuilder builder = new SqlFilterBuilder();
		List<String> excluded = List.of("view","source","field");
		
		for (var e : filters.entrySet()) {
		String fld = e.getKey().toLowerCase(), raw = e.getValue();
		if (raw==null||raw.isBlank()||excluded.contains(fld)) continue;
		String dbField = FIELD_ALIASES.getOrDefault(fld,fld);
		String col;
		try { col = getColumnForField(viewName, dbField); }
		catch (IllegalArgumentException ex) { continue; }
		
		if (numericFields.contains(col)) {
		builder.addNumericFilter(col, raw);
		} else if (raw.toUpperCase().startsWith("W")||raw.matches("\\d{4}-W.*")) {
		boolean isTextDate = TEXT_DATE_FIELDS.contains(col.toLowerCase(Locale.ROOT));
		builder.addWeekRangeFilter(col, raw, isTextDate);
		} else {
		builder.addTextFilter(col, raw);
		}
		}
		
		sql.append(builder.buildWhereClause())
		.append(" GROUP BY key ORDER BY key");
		
		var rows = jdbcTemplate.queryForList(sql.toString(), builder.getParams().toArray());
		for (var row: rows) {
		Object k = row.get("key"), v = row.get("value");
		result.put(k==null?"NULL":k.toString(), v);
		}
		return result;
		}
    // ---------------------------------------------------------
    // 5. Agregaciones con joins
    // ---------------------------------------------------------
    private Map<String, String> getAggregatorColumnsMap(String viewName) {
        Map<String, String> mapping = ViewColumnMappings.getAggregatorColumnsMap(viewName);
        if (mapping == null || mapping.isEmpty()) {
            throw new IllegalArgumentException("Vista no válida para agregación: " + viewName);
        }
        return mapping;
    }

    private String getAggregationFromClause(String viewName) {
        return switch (viewName.toLowerCase()) {
            case "contracts_list" ->
                "contracts_list c " +
                "LEFT JOIN task_buildings b ON c.building_id = b.assignment_number " +
                "LEFT JOIN task_units u     ON b.assignment_number = u.building_id " +
                "LEFT JOIN mastergrid m     ON m.Auftragsnummer   = u.assignment_number " +
                "LEFT JOIN outsources o     ON LOWER(c.access_location) = LOWER(o.AP)";
            case "buildings_status_extended" -> "vw_buildings_status_extended";
            case "baulist_highlights" -> "baulist_highlights";
            case "planner" -> "mastergrid";
            
            case "task_units" -> "task_units";
            case "buildings_task" -> "task_buildings";
            case "activations_highlights" -> "activations_highlights";
            case "ar_web" -> "ar_web";
            default -> throw new IllegalArgumentException("Vista no soportada para agregación: " + viewName);
        };
    }

    public List<Map<String, Object>> getContractsAggregation(String field1,
            String field2,
            Map<String, String> filters) {
				String viewName = filters.getOrDefault("view", "contracts_list");
				Map<String, String> aggMap = getAggregatorColumnsMap(viewName);
				
				String col1 = aggMap.get(field1.toLowerCase());
				String col2 = aggMap.get(field2.toLowerCase());
				if (col1 == null || col2 == null) {
				throw new IllegalArgumentException("Campo no válido para agregación");
				}
				
				String col1Sql = col1;
				String col2Sql = col2;
				if ("building_has_class".equalsIgnoreCase(col1)) {
				col1Sql = """
				CASE
				WHEN building_has_class ILIKE '%HAS1%' AND building_has_class ILIKE '%HAS2%' THEN 'HAS'
				WHEN building_has_class ILIKE '%HAS1%' THEN 'HAS1'
				WHEN building_has_class ILIKE '%HAS2%' THEN 'HAS2'
				WHEN building_has_class ILIKE '%HAS0%' THEN 'HAS0'
				ELSE 'SIN CLASIFICAR'
				END
				""";
				}
				if ("building_has_class".equalsIgnoreCase(col2)) {
				col2Sql = col1Sql;
				}
				
				StringBuilder sql = new StringBuilder(String.format("""
				SELECT %s AS field1, %s AS field2, COUNT(*) AS count
				FROM %s
				""", col1Sql, col2Sql, getAggregationFromClause(viewName)));
				
				SqlFilterBuilder builder = new SqlFilterBuilder();
				List<String> excluded = List.of("view","source","field1","field2");
				
				for (var e : filters.entrySet()) {
				String fld = e.getKey().toLowerCase();
				String raw = e.getValue();
				if (raw == null || raw.isBlank() || excluded.contains(fld)) continue;
				
				String dbField = aggMap.get(fld);
				if (dbField == null) continue;
				
				// Normalización HAS
				if (field1.equalsIgnoreCase("has_class") && fld.equals("has_class") && "HAS".equalsIgnoreCase(raw)) {
				builder.addTextFilter("building_has_class", "HAS1");
				builder.addTextFilter("building_has_class", "HAS2");
				continue;
				}
				
				String col = dbField;
				if (numericFields.contains(col)) {
				builder.addNumericFilter(col, raw);
				} else if (raw.toUpperCase().startsWith("W") || raw.matches("\\d{4}-W.*")) {
				boolean isTextDate = TEXT_DATE_FIELDS.contains(col.toLowerCase(Locale.ROOT));
				builder.addWeekRangeFilter(col, raw, isTextDate);
				} else {
				builder.addTextFilter(col, raw);
				}
				}
				
				sql.append(builder.buildWhereClause())
				.append(String.format(" GROUP BY %s, %s ORDER BY %s, %s", col1Sql, col2Sql, col1Sql, col2Sql));
				
				return jdbcTemplate.queryForList(sql.toString(), builder.getParams().toArray());
				}


    public List<Map<String, Object>> getHasAggregationByField(String groupField,
            Map<String, String> filters) {
				Map<String, String> allowed = ViewColumnMappings.getAggregatorColumnsMap("buildings_status_extended");
				String column = allowed.get(groupField.toLowerCase());
				if (column == null) {
				throw new IllegalArgumentException("Campo de agrupación no válido: " + groupField);
				}
				
				StringBuilder sql = new StringBuilder(String.format("""
				SELECT %s AS field,
				SUM(has0_count) AS has0,
				SUM(has1_count) AS has1,
				SUM(has2_count) AS has2
				FROM vw_buildings_status_extended
				""", column));
				
				SqlFilterBuilder builder = new SqlFilterBuilder();
				List<String> excluded = List.of("view","source","field1","field2","groupField");
				
				for (var e : filters.entrySet()) {
				String fld = e.getKey().toLowerCase();
				String raw = e.getValue();
				if (raw == null || raw.isBlank() || excluded.contains(fld)) continue;
				
				String dbCol = allowed.getOrDefault(fld, fld);
				if (numericFields.contains(dbCol)) {
				builder.addNumericFilter(dbCol, raw);
				} else if (raw.toUpperCase().startsWith("W") || raw.matches("\\d{4}-W.*")) {
				boolean isTextDate = TEXT_DATE_FIELDS.contains(dbCol.toLowerCase(Locale.ROOT));
				builder.addWeekRangeFilter(dbCol, raw, isTextDate);
				} else {
				builder.addTextFilter(dbCol, raw);
				}
				}
				
				sql.append(builder.buildWhereClause())
				.append(" GROUP BY ").append(column)
				.append(" ORDER BY ").append(column);
				
				return jdbcTemplate.queryForList(sql.toString(), builder.getParams().toArray());
				}
    // ---------------------------------------------------------
    // 6. Queries extendidas (vw_buildings_status_extended)
    // ---------------------------------------------------------
    public List<Map<String, Object>> getExtendedContractsReport(Map<String, String> filters, int offset, int limit) {
        String baseSql = "SELECT * FROM vw_buildings_status_extended";

        SqlFilterBuilder builder = new SqlFilterBuilder();
        builder.addTextFilter("building_id",               filters.get("building_id"));
        builder.addTextFilter("subtype",                  filters.get("subtype"));
        builder.addTextFilter("access_location",           filters.get("access_location"));
        builder.addTextFilter("aufgabenstatus",          filters.get("aufgabenstatus"));
        builder.addTextFilter("home_id",                   filters.get("home_id"));
        builder.addNumericFilter("pha",                    filters.get("pha"));
        builder.addTextFilter("building_status",           filters.get("building_status"));
        builder.addTextFilter("pha_reason",                filters.get("pha_reason"));
        builder.addTextFilter("building_has_class",        filters.get("has_class"));
        builder.addNumericFilter("has0_count",             filters.get("has0_count"));
        builder.addNumericFilter("has1_count",             filters.get("has1_count"));
        builder.addNumericFilter("has2_count",             filters.get("has2_count"));
        builder.addTextFilter("activeoperator_name_from_tri", filters.get("activeoperator_name_from_tri"));
        builder.addTextFilter("anschlussstatus_contracts", filters.get("anschlussstatus_contracts"));
        builder.addTextFilter("unit_statuses",             filters.get("unit_statuses"));
        builder.addTextFilter("grundna",           filters.get("grundna"));

        StringBuilder sql = new StringBuilder(baseSql)
            .append(builder.buildWhereClause())
            .append(" ORDER BY home_id, building_id");
        if (limit > 0) sql.append(" LIMIT ? OFFSET ?");

        List<Object> params = new ArrayList<>(builder.getParams());
        if (limit > 0) {
            params.add(limit);
            params.add(offset);
        }

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public int countExtendedContracts(Map<String, String> filters) {
        String baseSql = "SELECT COUNT(*) FROM vw_buildings_status_extended";

        SqlFilterBuilder builder = new SqlFilterBuilder();
        builder.addTextFilter("building_id",               filters.get("building_id"));
        builder.addNumericFilter("pha",                    filters.get("pha"));
        builder.addTextFilter("home_id",                   filters.get("home_id"));
        builder.addTextFilter("subtype",                  filters.get("subtype"));
        builder.addTextFilter("access_location",           filters.get("access_location"));
        builder.addTextFilter("aufgabenstatus",          filters.get("aufgabenstatus"));
        builder.addTextFilter("building_status",           filters.get("building_status"));
        builder.addTextFilter("pha_reason",                filters.get("pha_reason"));
        builder.addTextFilter("building_has_class",        filters.get("has_class"));
        builder.addNumericFilter("has0_count",             filters.get("has0_count"));
        builder.addNumericFilter("has1_count",             filters.get("has1_count"));
        builder.addNumericFilter("has2_count",             filters.get("has2_count"));
        builder.addTextFilter("unit_statuses",             filters.get("unit_statuses"));
        builder.addTextFilter("activeoperator_name_from_tri", filters.get("activeoperator_name_from_tri"));
        builder.addTextFilter("anschlussstatus",           filters.get("anschlussstatus"));
        builder.addTextFilter("weitere_susatz", filters.get("weitere_susatz"));
        builder.addExactTextFilter("anschlussstatus_contracts", filters.get("anschlussstatus_contracts"));

        String sql = baseSql + builder.buildWhereClause();
        return jdbcTemplate.queryForObject(sql, Integer.class, builder.getParams().toArray());
    }

}
