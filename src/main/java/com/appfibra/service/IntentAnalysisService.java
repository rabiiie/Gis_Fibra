package com.appfibra.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;

@Service
public class IntentAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(IntentAnalysisService.class);

    // Definimos patrones de palabras clave con expresiones regulares
    private static final Map<RequestType, Pattern> INTENT_PATTERNS = Map.of(
        RequestType.DATA_QUERY, Pattern.compile("\\b(cuántos|lista|detalles|mostrar|dónde|ubicación|cercano|dentro de|cantidad|número|existen|hay|total)\\b", Pattern.CASE_INSENSITIVE),
        RequestType.METADATA, Pattern.compile("\\b(esquema|tablas|columnas|metadatos|estructura)\\b", Pattern.CASE_INSENSITIVE)
    );

    /**
     * Analiza la pregunta del usuario para clasificarla como consulta de datos, metadatos o pregunta general.
     * 
     * @param question Pregunta del usuario en lenguaje natural.
     * @return Tipo de solicitud detectado.
     */
    public RequestType analyze(String question) {
        if (question == null || question.isBlank()) {
            logger.warn("Pregunta vacía o nula detectada.");
            return RequestType.GENERAL;
        }

        String lowerQuestion = question.toLowerCase();

        // Recorremos los patrones y verificamos si hay coincidencias
        for (var entry : INTENT_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(lowerQuestion).find()) {
                logger.info("Clasificación de pregunta: {} → {}", question, entry.getKey());
                return entry.getKey();
            }
        }

        // Si no coincide con ningún patrón, se trata como pregunta general
        logger.info("Pregunta no clasificada específicamente, asignada a GENERAL: {}", question);
        return RequestType.GENERAL;
    }

    public enum RequestType {
        METADATA,   // Solicita estructura de la BD
        DATA_QUERY, // Necesita ejecutar consulta
        GENERAL     // Pregunta genérica
    }
}
