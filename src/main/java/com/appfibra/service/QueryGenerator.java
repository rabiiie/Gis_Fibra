package com.appfibra.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class QueryGenerator {

    private static final Logger logger = LoggerFactory.getLogger(QueryGenerator.class);

    private final DeepSeekService deepSeekService;
    private final MetadataService metadataService;

    private static final String SQL_RULES = """
        🔹 **Estructura de la consulta**
        1. La salida debe ser **exclusivamente** una sentencia SELECT.
        2. No generes explicaciones en lenguaje natural, solo la consulta SQL.
        3. No incluyas múltiples sentencias ni punto y coma (;).
        4. No uses information_schema. Solo usa las tablas reales: civil_works, pops, projects, dps, streets, etc.
        5. Usa LIMIT 1000 para limitar resultados.
        
        🔹 **Relaciones importantes**
        - `civil_works` tiene la columna `pop_id` (FK a `pops.pop_id`).
        - `civil_works` tiene la columna `dp_id` (FK a `dps.dp_id`).
        - `civil_works` tiene la columna `street_id` (FK a `streets.street_id`).
        - `pops` tiene la columna `project_id` (FK a `projects.project_id`).
        
        🔹 **Reglas según la información que el usuario proporciona**
        6. **Si el usuario menciona un proyecto, filtra por `projects.name`**:
           ```sql
           WHERE p.name = 'Höxter Nord'
           ```
        7. **Si el usuario NO menciona un proyecto, NO filtres por `projects.name`**:
           ```sql
           WHERE pp.name = 'FBX003' 
             AND d.name = 'DP003'
           ```
        8. **Si el usuario menciona una calle, añade `LEFT JOIN streets` y filtra por `streets.name`**:
           ```sql
           LEFT JOIN streets s ON cw.street_id = s.street_id
           WHERE s.name = 'Fuchsbreite'
           ```

        🔹 **Manejo de tipos de obra civil**
        9. Cuando se consulte por un tipo específico de obra civil, verifica:
            - Primero, busca en `cw.type` con igualdad: `cw.type = 'Asphalt'`
            - Si no hay resultados en `cw.type`, busca en `cw.spec` con `LIKE '%Asphalt%'`
        10. Usa la condición:
           ```sql
           WHERE (cw.type = 'Asphalt' OR cw.spec LIKE '%Asphalt%')
           ```

        🔹 **Cálculos y alias**
        11. Para contar o sumar metros de obra civil, usa SUM(cw.length_meters) con alias claro, 
           por ejemplo: SUM(cw.length_meters) AS total_asphalt.

        🔹 **Ejemplo de consulta correcta si el usuario menciona una calle**
        **Pregunta:** "¿Cuántos metros de asfalto hay en la calle Fuchsbreite del DP003 del PoP FBX003?"  
        **Consulta esperada:**
        ```sql
        SELECT SUM(cw.length_meters) AS total_asphalt
        FROM civil_works cw
        LEFT JOIN pops pp ON cw.pop_id = pp.pop_id
        LEFT JOIN dps d ON cw.dp_id = d.dp_id
        LEFT JOIN streets s ON cw.street_id = s.street_id
        LEFT JOIN projects p ON pp.project_id = p.project_id
        WHERE pp.name = 'FBX003'
          AND d.name = 'DP003'
          AND s.name = 'Fuchsbreite'
          AND (cw.type = 'Asphalt' OR cw.spec LIKE '%Asphalt%')
        LIMIT 1000;
        ```

        🔹 **Ejemplo de consulta correcta si el usuario NO menciona la calle**
        **Pregunta:** "¿Cuántos metros de asfalto tiene el DP003 del PoP FBX003?"  
        **Consulta esperada:**
        ```sql
        SELECT SUM(cw.length_meters) AS total_asphalt
        FROM civil_works cw
        LEFT JOIN pops pp ON cw.pop_id = pp.pop_id
        LEFT JOIN dps d ON cw.dp_id = d.dp_id
        LEFT JOIN projects p ON pp.project_id = p.project_id
        WHERE pp.name = 'FBX003'
          AND d.name = 'DP003'
          AND (cw.type = 'Asphalt' OR cw.spec LIKE '%Asphalt%')
        LIMIT 1000;
        ```
    """;

    public QueryGenerator(DeepSeekService deepSeekService, MetadataService metadataService) {
        this.deepSeekService = deepSeekService;
        this.metadataService = metadataService;
    }

    public String generateSqlFromQuestion(String question) {
        logger.info("Generando SQL para la pregunta: {}", question);

        String schemaContext = metadataService.getFormattedMetadata();

        String systemPrompt = String.format("""
            Eres un experto en PostgreSQL y PostGIS. Solo puedes generar consultas SQL válidas para responder preguntas.
            
            --- CONTEXTO DE LA BASE DE DATOS ---
            %s
            
            --- REGLAS OBLIGATORIAS ---
            %s
            
            --- PREGUNTA DEL USUARIO ---
            %s
            """, schemaContext, SQL_RULES, question);

        String response = deepSeekService.generateResponse(systemPrompt).block();
        if (response == null || response.isBlank()) {
            logger.error("DeepSeek no devolvió una respuesta válida.");
            throw new SqlGenerationException("DeepSeek no devolvió respuesta para la generación de SQL.");
        }

        response = cleanSqlResponse(response);
        validateSqlResponse(response);

        logger.info("SQL generado correctamente.");
        return response;
    }

    private String cleanSqlResponse(String response) {
        return response.replace("```sql", "").replace("```", "").trim();
    }

    private void validateSqlResponse(String sql) {
        String lower = sql.toLowerCase();
        if (!lower.startsWith("select")) {
            logger.error("Respuesta inválida, no es una consulta SQL: {}", sql);
            throw new SqlGenerationException("ERROR: La respuesta no es una consulta SQL válida.");
        }
        if (lower.contains(";")) {
            logger.error("Respuesta inválida, contiene múltiples sentencias: {}", sql);
            throw new SqlGenerationException("ERROR: La respuesta contiene múltiples sentencias SQL.");
        }
    }

    public static class SqlGenerationException extends RuntimeException {
        public SqlGenerationException(String message) {
            super(message);
        }
    }
}
