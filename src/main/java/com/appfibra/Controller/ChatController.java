package com.appfibra.Controller;

import com.appfibra.service.ChatbotService;
import com.appfibra.service.ChatMemoryService;
import com.appfibra.service.DeepSeekService;
import com.appfibra.service.IntentAnalysisService;
import com.appfibra.service.QueryGenerator;
import com.appfibra.service.QueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final IntentAnalysisService intentService;
    private final QueryGenerator queryGenerator;
    private final QueryService queryService;
    private final DeepSeekService deepSeekService;
    private final ChatbotService chatbotService;
    private final ChatMemoryService chatMemoryService;

    public ChatController(IntentAnalysisService intentService,
                          QueryGenerator queryGenerator,
                          QueryService queryService,
                          DeepSeekService deepSeekService,
                          ChatbotService chatbotService,
                          ChatMemoryService chatMemoryService) {
        this.intentService = intentService;
        this.queryGenerator = queryGenerator;
        this.queryService = queryService;
        this.deepSeekService = deepSeekService;
        this.chatbotService = chatbotService;
        this.chatMemoryService = chatMemoryService;
    }

    @PostMapping
    public Mono<ResponseEntity<ChatResponse>> handleQuestion(@RequestBody ChatRequest request) {
        if (request == null || request.question() == null || request.question().trim().isEmpty()) {
            return Mono.just(badRequest().body(new ChatResponse("Empty or null question", false, List.of(), null)));
        }

        String question = request.question().trim();
        String sessionId = (request.sessionId() != null ? request.sessionId() : "defaultSession");

        // 1) Buscar si existe una respuesta previa / fuzzy match en la base
        return chatbotService.findExistingAnswer(question)
            .map(dbAnswer -> ok(new ChatResponse(dbAnswer, true, List.of("database"), sessionId)))
            // 2) Si no hay match, manejar como pregunta nueva
            .switchIfEmpty(Mono.defer(() -> handleNewQuestion(question, sessionId)))
            // 3) Manejo de errores
            .onErrorResume(err -> Mono.just(internalError("Internal error: " + err.getMessage(), sessionId)));
    }

    private Mono<ResponseEntity<ChatResponse>> handleNewQuestion(String question, String sessionId) {
        // Clasificamos la intención (METADATA / DATA_QUERY / GENERAL)
        IntentAnalysisService.RequestType intent = intentService.analyze(question);
        return switch (intent) {
            case METADATA -> handleMetadataQuestion(question, sessionId);
            case DATA_QUERY -> handleDataQuestion(question, sessionId);
            case GENERAL -> handleGeneralQuestion(question, sessionId);
        };
    }

    private Mono<ResponseEntity<ChatResponse>> handleMetadataQuestion(String question, String sessionId) {
        return deepSeekService.generateResponse("El usuario solicita metadatos:\n" + question)
            .flatMap(aiResponse -> {
                chatbotService.saveConversation(question, aiResponse); 
                return Mono.just(ok(new ChatResponse(aiResponse, false, List.of("metadata"), sessionId)));
            });
    }

    private Mono<ResponseEntity<ChatResponse>> handleDataQuestion(String question, String sessionId) {
        // Generamos SQL con la ayuda de DeepSeek
        return Mono.fromCallable(() -> queryGenerator.generateSqlFromQuestion(question))
            // Ejecutamos la consulta
            .flatMap(sql -> Mono.fromCallable(() -> queryService.executeQuery(sql)))
            // Llamamos a DeepSeek para que interprete datos y genere respuesta en español (markdown)
            .flatMap(queryResult -> deepSeekService.generateDataResponse(queryResult, question))
            .flatMap(aiResponse -> {
                chatbotService.saveConversation(question, aiResponse); 
                return Mono.just(ok(new ChatResponse(aiResponse, false, List.of("database"), sessionId)));
            });
    }

    private Mono<ResponseEntity<ChatResponse>> handleGeneralQuestion(String question, String sessionId) {
        // Construir el contexto a partir del historial de mensajes
        String context = chatMemoryService.buildContext(sessionId);
        // Crear un prompt que incluya el contexto anterior y la pregunta actual
        String promptWithContext = context + "\n\nPregunta actual: " + question;
        
        return deepSeekService.generateResponse(promptWithContext)
            .flatMap(aiResponse -> {
                // Guardamos la nueva conversación en la base de datos y en la memoria
                chatbotService.saveConversation(question, aiResponse);
                chatMemoryService.addMessage(sessionId, "assistant", aiResponse);
                return Mono.just(ok(new ChatResponse(aiResponse, false, List.of("general"), sessionId)));
            });
    }


    @GetMapping("/history")
    public Mono<ResponseEntity<List<ChatbotService.ChatHistory>>> getHistory(@RequestParam(defaultValue = "10") int limit) {
        return Mono.fromSupplier(() -> chatbotService.getChatHistory(limit))
            .map(ResponseEntity::ok)
            .onErrorResume(err -> Mono.just(ResponseEntity.internalServerError().build()));
    }

    // Opcional: ver el historial en memoria (dentro de la sesión)
    @GetMapping("/memory")
    public Mono<ResponseEntity<List<ChatMemoryService.Message>>> getMemory(@RequestParam String sessionId) {
        return Mono.fromSupplier(() -> chatMemoryService.getSessionMessages(sessionId))
            .map(ResponseEntity::ok)
            .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().build()));
    }

    private ResponseEntity<ChatResponse> internalError(String message, String sessionId) {
        return ResponseEntity.internalServerError()
            .body(new ChatResponse(message, false, List.of(), sessionId));
    }

    public record ChatRequest(String question, String sessionId) {}
    public record ChatResponse(String answer, boolean cached, List<String> sources, String sessionId) {}
}
