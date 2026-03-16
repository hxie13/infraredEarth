package cn.ac.sitp.infrared.ai.llm;

import cn.ac.sitp.infrared.ai.model.ChatMessage;
import cn.ac.sitp.infrared.ai.model.ToolDefinition;

import java.util.List;

/**
 * Interface for LLM API calls. Supports chat completions with optional tool/function calling.
 */
public interface LlmClient {

    /**
     * Send messages to the LLM, optionally with tool definitions for function calling.
     *
     * @param messages conversation messages
     * @param tools    tool definitions (may be null or empty for plain chat)
     * @return LLM response containing either text content or tool calls
     */
    LlmResponse chat(List<ChatMessage> messages, List<ToolDefinition> tools);
}
