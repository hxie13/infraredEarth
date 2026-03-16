package cn.ac.sitp.infrared.ai.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Outgoing chat response to the frontend.
 */
public class ChatResponse {

    private String reply;
    private Map<String, Object> data;
    private List<VisualizationAction> actions = new ArrayList<>();
    private String conversationId;
    private String traceId;

    public ChatResponse() {}

    public static ChatResponse withReply(String reply) {
        ChatResponse r = new ChatResponse();
        r.setReply(reply);
        return r;
    }

    public ChatResponse addAction(VisualizationAction action) {
        this.actions.add(action);
        return this;
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
    public List<VisualizationAction> getActions() { return actions; }
    public void setActions(List<VisualizationAction> actions) { this.actions = actions; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}
