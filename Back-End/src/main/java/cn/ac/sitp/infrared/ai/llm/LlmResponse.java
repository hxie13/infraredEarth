package cn.ac.sitp.infrared.ai.llm;

import cn.ac.sitp.infrared.ai.model.ToolCall;

import java.util.List;

/**
 * Response from an LLM API call.
 */
public class LlmResponse {

    private String content;
    private List<ToolCall> toolCalls;

    public LlmResponse() {}

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    public static LlmResponse text(String content) {
        LlmResponse r = new LlmResponse();
        r.setContent(content);
        return r;
    }

    public static LlmResponse withToolCalls(List<ToolCall> toolCalls) {
        LlmResponse r = new LlmResponse();
        r.setToolCalls(toolCalls);
        return r;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<ToolCall> getToolCalls() { return toolCalls; }
    public void setToolCalls(List<ToolCall> toolCalls) { this.toolCalls = toolCalls; }
}
