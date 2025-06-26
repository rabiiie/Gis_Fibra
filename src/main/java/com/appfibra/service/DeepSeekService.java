package com.appfibra.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class DeepSeekService {

    private final WebClient webClient;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.key}")
    private String apiKey;

    public DeepSeekService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Envía un prompt a la API de DeepSeek y devuelve la respuesta (Mono<String>).
     */
    public Mono<String> generateResponse(String prompt) {
        Map<String, Object> requestBody = Map.of(
            "model", "deepseek-chat",
            "messages", List.of(Map.of("role", "user", "content", prompt)),
            "temperature", 0.7,
            "max_tokens", 1000
        );

        return webClient.post()
            .uri(apiUrl)
            .header("Authorization", "Bearer " + apiKey)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Map.class)
            .map(this::extractDeepSeekResponse)
            .onErrorResume(e -> Mono.just("Error al comunicarse con la API de DeepSeek: " + e.getMessage()));
    }

    /**
     * Extrae el contenido de la respuesta de DeepSeek.
     */
    private String extractDeepSeekResponse(Map<String, Object> response) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null && message.containsKey("content")) {
                    return message.get("content").toString();
                }
            }
            return "No se recibió respuesta de DeepSeek.";
        } catch (Exception e) {
            return "Error al procesar la respuesta: " + e.getMessage();
        }
    }

    /**
     * Genera una respuesta en base a un contexto de datos y la pregunta.
     */
    public Mono<String> generateDataResponse(String dataContext, String question) {
        String fullPrompt = String.format("""
            Eres un analista de datos. Usa esta información:
            %s

            Pregunta: %s

            Instrucciones:
            1. Respuesta en español
            2. Usar formato markdown
            3. Mostrar números relevantes
            4. Ser conciso pero informativo
            5. Si hay datos geoespaciales, mencionar coordenadas
            6. Si son mas de 1000 metros muestra datos en Kilometros
            """, dataContext, question);

        return generateResponse(fullPrompt);
    }
    
}
