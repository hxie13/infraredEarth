package cn.ac.sitp.infrared.ai.model;

import com.alibaba.fastjson2.JSONObject;

import java.util.List;

/**
 * Represents a single message in a conversation (OpenAI-compatible format).
 */
public class ChatMessage {

    private String role;       // "system", "user", "assistant", "tool"
    private String content;
    private List<ToolCall> toolCalls;   // present when role=assistant and LLM wants to call tools
    private String toolCallId;          // present when role=tool (response to a tool call)
    private String name;                // tool name when role=tool

    public ChatMessage() {}

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static ChatMessage system(String content) {
        return new ChatMessage("system", content);
    }

    public static ChatMessage user(String content) {
        return new ChatMessage("user", content);
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content);
    }

    public static ChatMessage assistantWithToolCalls(List<ToolCall> toolCalls) {
        ChatMessage msg = new ChatMessage("assistant", null);
        msg.setToolCalls(toolCalls);
        return msg;
    }

    public static ChatMessage toolResult(String toolCallId, String name, String content) {
        ChatMessage msg = new ChatMessage("tool", content);
        msg.setToolCallId(toolCallId);
        msg.setName(name);
        return msg;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("role", role);
        if (content != null) {
            json.put("content", content);
        }
        if (toolCalls != null && !toolCalls.isEmpty()) {
            json.put("tool_calls", toolCalls.stream().map(ToolCall::toJson).toList());
        }
        if (toolCallId != null) {
            json.put("tool_call_id", toolCallId);
        }
        if (name != null) {
            json.put("name", name);
        }
        return json;
    }

    // Getters and setters
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<ToolCall> getToolCalls() { return toolCalls; }
    public void setToolCalls(List<ToolCall> toolCalls) { this.toolCalls = toolCalls; }
    public String getToolCallId() { return toolCallId; }
    public void setToolCallId(String toolCallId) { this.toolCallId = toolCallId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
