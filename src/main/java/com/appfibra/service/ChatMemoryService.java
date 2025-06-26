package com.appfibra.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map;

@Service
public class ChatMemoryService {

    private final Map<String, List<Message>> memory = new ConcurrentHashMap<>();

    public record Message(String role, String content) {}

    /**
     * Devuelve la lista de mensajes de una sesión dada. 
     * Si no existe, la crea vacía.
     */
    public List<Message> getSessionMessages(String sessionId) {
        return memory.computeIfAbsent(sessionId, k -> new ArrayList<>());
    }

    /**
     * Agrega un mensaje (rol y contenido) al historial de la sesión.
     */
    public void addMessage(String sessionId, String role, String content) {
        List<Message> sessionMessages = getSessionMessages(sessionId);
        sessionMessages.add(new Message(role, content));
    }

    /**
     * Convierte el historial a un string concatenado (por si quieres usarlo como contexto).
     */
    public String buildContext(String sessionId) {
        List<Message> sessionMessages = getSessionMessages(sessionId);
        return sessionMessages.stream()
            .map(m -> m.role() + ": " + m.content())
            .collect(Collectors.joining("\n"));
    }

    /**
     * Limpia la sesión
     */
    public void clearSession(String sessionId) {
        memory.remove(sessionId);
    }
}
