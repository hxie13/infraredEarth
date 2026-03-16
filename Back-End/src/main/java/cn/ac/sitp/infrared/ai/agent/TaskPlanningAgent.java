package cn.ac.sitp.infrared.ai.agent;

import cn.ac.sitp.infrared.ai.llm.LlmClient;
import cn.ac.sitp.infrared.ai.llm.LlmResponse;
import cn.ac.sitp.infrared.ai.model.ChatMessage;
import cn.ac.sitp.infrared.ai.model.ExecutionPlan;
import cn.ac.sitp.infrared.ai.model.PlanStep;
import cn.ac.sitp.infrared.ai.tool.ToolRegistry;
import cn.ac.sitp.infrared.ai.workflow.WorkflowMatcher;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Plans multi-step task execution workflows.
 * Uses ToolRegistry for accurate tool catalog and WorkflowMatcher for template-based planning.
 * Falls back to LLM planning when no template matches.
 */
@Component
public class TaskPlanningAgent {

    private static final Logger log = LoggerFactory.getLogger(TaskPlanningAgent.class);

    @Autowired
    private ToolRegistry toolRegistry;

    @Autowired
    private WorkflowMatcher workflowMatcher;

    private static final String SYSTEM_PROMPT_HEADER = """
            你是红外地球遥感平台的任务规划器。你的职责是将用户的自然语言请求分解为有序的执行计划。

            """;

    private static final String SYSTEM_PROMPT_FOOTER = """

            ## 输出格式

            严格返回以下 JSON 格式，不要包含其他文字：
            {
              "steps": [
                {"agent": "AgentName", "tool": "tool_name", "args": {"key": "value"}},
                ...
              ]
            }

            args 中的值可以引用前面步骤的结果，用 $step_N 表示（N为步骤序号，从1开始）。
            例如 {"nc_ids": "$step_1"} 表示使用第1步的结果。

            如果用户请求很简单（单步），也要生成计划，只是步骤较少。
            请根据工具的"适用"和"不适用"说明来选择正确的工具。
            """;

    /**
     * Build the full system prompt dynamically from ToolRegistry and WorkflowMatcher.
     */
    private String buildSystemPrompt() {
        return SYSTEM_PROMPT_HEADER
                + toolRegistry.buildPlannerCatalogPrompt()
                + workflowMatcher.buildWorkflowCatalogPrompt()
                + SYSTEM_PROMPT_FOOTER;
    }

    /**
     * Generate an execution plan for the given intent and user message.
     * Strategy: try workflow template match first, then fall back to LLM.
     */
    public ExecutionPlan plan(LlmClient llmClient, AgentIntent intent,
                              List<AgentIntent> subIntents, List<ChatMessage> history) {

        // Extract the latest user message for template matching
        String userQuery = extractLastUserMessage(history);

        // Step 1: Try workflow template matching (no LLM call needed)
        ExecutionPlan templatePlan = workflowMatcher.match(userQuery);
        if (templatePlan != null) {
            log.info("Using workflow template plan: {} steps", templatePlan.getSteps().size());
            return templatePlan;
        }

        // Step 2: Fall back to LLM-based planning
        log.info("No template match, using LLM planning");
        return planWithLlm(llmClient, history);
    }

    private ExecutionPlan planWithLlm(LlmClient llmClient, List<ChatMessage> history) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(buildSystemPrompt()));

        // Include recent conversation for context
        int start = Math.max(0, history.size() - 6);
        for (int i = start; i < history.size(); i++) {
            ChatMessage msg = history.get(i);
            if ("user".equals(msg.getRole()) || "assistant".equals(msg.getRole())) {
                messages.add(msg);
            }
        }

        LlmResponse response = llmClient.chat(messages, null);
        return parsePlan(response.getContent());
    }

    private ExecutionPlan parsePlan(String content) {
        ExecutionPlan plan = new ExecutionPlan();
        if (content == null || content.isBlank()) {
            return plan;
        }

        try {
            String json = content.trim();
            if (json.contains("{")) {
                json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
            }
            JSONObject obj = JSON.parseObject(json);
            JSONArray steps = obj.getJSONArray("steps");

            if (steps != null) {
                for (int i = 0; i < steps.size(); i++) {
                    JSONObject stepJson = steps.getJSONObject(i);
                    PlanStep step = new PlanStep(
                            stepJson.getString("agent"),
                            stepJson.getString("tool"),
                            stepJson.getJSONObject("args")
                    );
                    plan.addStep(step);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse execution plan: {}", content, e);
        }

        return plan;
    }

    private String extractLastUserMessage(List<ChatMessage> history) {
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatMessage msg = history.get(i);
            if ("user".equals(msg.getRole()) && msg.getContent() != null) {
                return msg.getContent();
            }
        }
        return "";
    }
}
