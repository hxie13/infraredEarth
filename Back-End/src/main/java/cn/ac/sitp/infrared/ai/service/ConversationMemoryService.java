package cn.ac.sitp.infrared.ai.service;

import cn.ac.sitp.infrared.ai.config.AiConfig;
import cn.ac.sitp.infrared.ai.model.ChatMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-session conversation history stored in HttpSession.
 */
@Service
public class ConversationMemoryService {

    private static final String SESSION_KEY = "infrared.ai.conversations";

    private final int maxHistory;

    public ConversationMemoryService(AiConfig aiConfig) {
        this.maxHistory = aiConfig.getConversation().getMaxHistory();
    }

    /**
     * Get or create a conversation by ID. Returns a mutable list of messages.
     */
    @SuppressWarnings("unchecked")
    public List<ChatMessage> getConversation(HttpServletRequest request, String conversationId) {
        HttpSession session = request.getSession(true);
        Map<String, List<ChatMessage>> conversations =
                (Map<String, List<ChatMessage>>) session.getAttribute(SESSION_KEY);

        if (conversations == null) {
            conversations = new ConcurrentHashMap<>();
            session.setAttribute(SESSION_KEY, conversations);
        }

        return conversations.computeIfAbsent(conversationId, k -> new ArrayList<>());
    }

    /**
     * Add a message and trim history if exceeding max.
     */
    public void addMessage(List<ChatMessage> conversation, ChatMessage message) {
        conversation.add(message);
        trimHistory(conversation);
    }

    /**
     * Generate a new conversation ID.
     */
    public String newConversationId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private void trimHistory(List<ChatMessage> conversation) {
        while (conversation.size() > maxHistory) {
            conversation.removeFirst();
        }
    }
}
