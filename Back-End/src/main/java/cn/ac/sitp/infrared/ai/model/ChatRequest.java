package cn.ac.sitp.infrared.ai.model;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * Incoming chat request from the frontend.
 */
public class ChatRequest {

    private String message;

    @JSONField(name = "conversation_id")
    private String conversationId;

    public ChatRequest() {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
}
