// Current user and time information
const CURRENT_USER = 'aa000031';
const CURRENT_DATE = new Date('2025-05-27 06:57:27Z');

document.addEventListener("DOMContentLoaded", function () {
    // Load chat history when modal is shown
    const deepSeekChatModal = document.getElementById('deepSeekChatModal');
    if (deepSeekChatModal) {
        deepSeekChatModal.addEventListener('show.bs.modal', async () => {
            await loadChatHistory();
        });
    }

    // Mostrar modal y cargar historial al hacer clic en el botón de navegación
    document.getElementById('nav-chat')?.addEventListener('click', () => {
        const modal = new bootstrap.Modal(document.getElementById('deepSeekChatModal'));
        modal.show();
    });

    // Permitir enviar mensaje al presionar la tecla Enter (sin Shift)
    document.getElementById('chatPrompt')?.addEventListener('keydown', function (event) {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            document.getElementById('sendChatPrompt')?.click();
        }
    });

    // Enviar mensaje al hacer clic en el botón "Enviar"
    document.getElementById('sendChatPrompt')?.addEventListener('click', async () => {
        const prompt = document.getElementById('chatPrompt')?.value.trim();
        if (!prompt) return;

        // Mostrar mensaje del usuario
        appendMessage("Usuario", prompt, "user-message", false);
        if (document.getElementById('chatPrompt')) {
            document.getElementById('chatPrompt').value = "";
        }

        // Mostrar animación de "Pensando"
        const thinkingElement = showThinkingAnimation();

        try {
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ question: prompt })
            });

            if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);

            const data = await response.json();
            // Ocultar animación de "Pensando"
            hideThinkingAnimation(thinkingElement);

            // Mostrar respuesta de DeepSeek
            appendMessage("DeepSeek", data.answer, "bot-message", true);

            // No need to reload history here since we just added the messages
        } catch (err) {
            hideThinkingAnimation(thinkingElement);
            appendMessage("Error", "Hubo un problema con el Chat: " + err.message, "text-danger", false);
        }
    });
});

// Función para cargar el historial de chat
async function loadChatHistory() {
    const chatHistory = document.getElementById('chatHistory');
    if (!chatHistory) return;

    try {
        chatHistory.innerHTML = `
            <div class="d-flex justify-content-center">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Cargando...</span>
                </div>
            </div>
        `;

        const res = await fetch('/api/chat/history');
        if (!res.ok) throw new Error(`Error ${res.status}: ${res.statusText}`);

        const history = await res.json();
        chatHistory.innerHTML = ""; // Clear loading spinner

        if (history && Array.isArray(history)) {
            // Se asume que el backend devuelve campos "question" y "answer"
            history.reverse().forEach(entry => {
                if (entry.question) appendMessage("Usuario", entry.question, "user-message", false);
                if (entry.answer) appendMessage("DeepSeek", entry.answer, "bot-message", true);
            });
        }

        // Scroll to bottom after loading history
        chatHistory.scrollTop = chatHistory.scrollHeight;
    } catch (err) {
        chatHistory.innerHTML = ""; // Clear loading spinner
        appendMessage("Error", "No se pudo cargar el historial: " + err.message, "text-danger", false);
    }
}

/**
 * Agrega un mensaje al historial.
 * @param {string} user - El nombre del emisor (por ejemplo, "Usuario" o "DeepSeek").
 * @param {string} text - El contenido del mensaje.
 * @param {string} className - Clase CSS para definir el estilo del mensaje.
 * @param {boolean} isMarkdown - Si es true, se convierte el texto de Markdown a HTML.
 */
function appendMessage(user, text, className, isMarkdown) {
    if (!text) return;
    const container = document.getElementById('chatHistory');
    if (!container) return;

    const div = document.createElement('div');
    div.classList.add("chat-message", className);

    if (isMarkdown) {
        const converter = new showdown.Converter();
        text = converter.makeHtml(text);
    }

    div.innerHTML = `<strong>${user}:</strong><br>${text}`;
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
}

// Mostrar la animación de "Pensando"
function showThinkingAnimation() {
    const container = document.getElementById('chatHistory');
    if (!container) return null;

    const thinkingElement = document.createElement('div');
    thinkingElement.classList.add("thinking-animation");
    thinkingElement.innerHTML = `
        <div class="thinking-dot"></div>
        <div class="thinking-dot"></div>
        <div class="thinking-dot"></div>
    `;

    container.appendChild(thinkingElement);
    return thinkingElement;
}

// Ocultar la animación de "Pensando"
function hideThinkingAnimation(thinkingElement) {
    if (thinkingElement) thinkingElement.remove();
}