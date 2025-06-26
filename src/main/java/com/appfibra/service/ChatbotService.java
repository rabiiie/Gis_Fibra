package com.appfibra.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ChatbotService {

    private final JdbcTemplate jdbcTemplate;
    private final ChatMemoryService chatMemoryService;

    public ChatbotService(JdbcTemplate jdbcTemplate, ChatMemoryService chatMemoryService) {
        this.jdbcTemplate = jdbcTemplate;
        this.chatMemoryService = chatMemoryService;
    }

    /**
     * Representa un registro de historial para la BD
     */
    public static record ChatHistory(
        Long id,
        String question,
        String answer
    ) {}

    /**
     * Intenta encontrar una respuesta previa que sea exacta o muy parecida a la pregunta.
     * Retorna un Mono vacío si no encuentra nada.
     * 
     * Esta implementación es MUY básica (exact match). 
     * Podrías mejorarla usando un índice full-text o fuzzy matching.
     */
    public Mono<String> findExistingAnswer(String question) {
        String sql = "SELECT answer FROM chat_history WHERE question = ? LIMIT 1";
        List<String> answers = jdbcTemplate.queryForList(sql, String.class, question);
        if (answers.isEmpty()) {
            return Mono.empty(); // no hay coincidencia
        }
        return Mono.just(answers.get(0));
    }

    /**
     * Guarda en la tabla chat_history y también en memoria de la sesión (si lo deseas).
     */
    public void saveConversation(String question, String answer) {
        // Guardar en BD
        String sqlInsert = "INSERT INTO chat_history (question, answer) VALUES (?, ?)";
        jdbcTemplate.update(sqlInsert, question, answer);

        // (Opcional) Guardar en memoria
        // Por simplicidad, se usa un sessionId fijo, o podrías pasarlo como parámetro
        String sessionId = "defaultSession";
        chatMemoryService.addMessage(sessionId, "user", question);
        chatMemoryService.addMessage(sessionId, "assistant", answer);
    }

    /**
     * Retorna el historial completo de la tabla chat_history, limitando la cantidad.
     */
    public List<ChatHistory> getChatHistory(int limit) {
        String sql = "SELECT id, question, answer FROM chat_history ORDER BY id DESC LIMIT ?";
        return jdbcTemplate.query(sql, rs -> {
            var list = new java.util.ArrayList<ChatHistory>();
            while (rs.next()) {
                list.add(new ChatHistory(
                    rs.getLong("id"),
                    rs.getString("question"),
                    rs.getString("answer")
                ));
            }
            return list;
        }, limit);
    }

}

