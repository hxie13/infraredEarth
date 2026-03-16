package cn.ac.sitp.infrared.ai.service;

import cn.ac.sitp.infrared.ai.agent.*;
import cn.ac.sitp.infrared.ai.context.TaskContext;
import cn.ac.sitp.infrared.ai.llm.LlmClient;
import cn.ac.sitp.infrared.ai.llm.LlmClientFactory;
import cn.ac.sitp.infrared.ai.model.*;
import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Main orchestration service for the multi-agent AI chat system.
 * Implements the Intent → Plan → Execute → Visualize pipeline
 * with TaskContext for state management and ExecutionTrace for auditing.
 */
@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    @Autowired
    private LlmClientFactory llmClientFactory;

    @Autowired
    private ConversationMemoryService memoryService;

    @Autowired
    private ExecutionTraceService traceService;

    @Autowired
    private OrchestratorAgent orchestratorAgent;

    @Autowired
    private TaskPlanningAgent taskPlanningAgent;

    @Autowired
    private PlanExecutor planExecutor;

    @Autowired
    private DataSearchAgent dataSearchAgent;

    @Autowired
    private ModelAnalysisAgent modelAnalysisAgent;

    @Autowired
    private StatisticsAgent statisticsAgent;

    @Autowired
    private VisualizationAgent visualizationAgent;

    @Autowired
    private ConversationalAgent conversationalAgent;

    /**
     * Process a user message through the full agent pipeline.
     */
    public ChatResponse processMessage(String message, String conversationId,
                                       AxrrAccount user, HttpServletRequest request) {
        LlmClient llmClient = llmClientFactory.getClient();

        // Manage conversation
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = memoryService.newConversationId();
        }
        List<ChatMessage> history = memoryService.getConversation(request, conversationId);

        // Add user message to history
        ChatMessage userMessage = ChatMessage.user(message);
        memoryService.addMessage(history, userMessage);

        // Create TaskContext for this request
        TaskContext context = new TaskContext(message, null);

        try {
            // Step 1: Intent classification
            OrchestratorAgent.IntentResult intentResult = orchestratorAgent.classify(llmClient, history);
            log.info("Intent classified: {} (sub: {})", intentResult.intent(), intentResult.subIntents());

            context.setIntent(intentResult.intent());
            context.getTrace().setClassifiedIntent(intentResult.intent());

            // Step 2: Route based on intent
            ChatResponse response;

            if (intentResult.intent() == AgentIntent.MULTI_STEP) {
                response = handleMultiStep(llmClient, intentResult, history, user, context);
            } else {
                response = handleSingleIntent(llmClient, intentResult.intent(), history, user, context);
            }

            response.setConversationId(conversationId);
            response.setTraceId(context.getTrace().getTraceId());

            // Add assistant reply to history
            if (response.getReply() != null) {
                memoryService.addMessage(history, ChatMessage.assistant(response.getReply()));
            }

            // Persist execution trace asynchronously
            context.getTrace().complete(
                    response.getReply() != null && !response.getReply().contains("失败") ? "SUCCESS" : "PARTIAL");
            Long userId = user != null ? (Long) user.getUserno() : null;
            traceService.saveTrace(context.getTrace(), userId);

            return response;

        } catch (Exception e) {
            log.error("AI chat processing failed", e);
            context.getTrace().complete("FAILED");
            Long userId = user != null ? (Long) user.getUserno() : null;
            traceService.saveTrace(context.getTrace(), userId);

            ChatResponse errorResponse = ChatResponse.withReply("处理您的请求时遇到了问题，请稍后重试。");
            errorResponse.setConversationId(conversationId);
            return errorResponse;
        }
    }

    private ChatResponse handleMultiStep(LlmClient llmClient, OrchestratorAgent.IntentResult intentResult,
                                         List<ChatMessage> history, AxrrAccount user, TaskContext context) {
        // Generate execution plan (tries template match first, then LLM)
        ExecutionPlan plan = taskPlanningAgent.plan(
                llmClient, intentResult.intent(), intentResult.subIntents(), history);

        log.info("Generated plan: {}", plan);

        if (plan.getSteps().isEmpty()) {
            return conversationalAgent.execute(llmClient, history);
        }

        // Execute plan with TaskContext
        ChatResponse planResult = planExecutor.execute(plan, user, context);

        // Use LLM to generate a natural language summary
        if (planResult.getData() != null || (planResult.getReply() != null && !planResult.getReply().isBlank())) {
            String executionSummary = planResult.getReply();
            List<ChatMessage> summaryMessages = new java.util.ArrayList<>(history);
            summaryMessages.add(ChatMessage.system(
                    "以下是任务执行结果，请用中文自然语言为用户总结：\n" + executionSummary));
            cn.ac.sitp.infrared.ai.llm.LlmResponse polished = llmClient.chat(summaryMessages, null);
            if (polished.getContent() != null && !polished.getContent().isBlank()) {
                planResult.setReply(polished.getContent());
            }
        }

        return planResult;
    }

    private ChatResponse handleSingleIntent(LlmClient llmClient, AgentIntent intent,
                                            List<ChatMessage> history, AxrrAccount user,
                                            TaskContext context) {
        return switch (intent) {
            case DATA_SEARCH -> {
                ChatResponse searchResult = dataSearchAgent.execute(llmClient, history);
                // Append visualization actions for search results
                if (searchResult.getData() != null) {
                    List<VisualizationAction> suggested = visualizationAgent.suggestActionsForData(searchResult.getData());
                    if (!suggested.isEmpty()) {
                        searchResult.getActions().addAll(suggested);
                    }
                    // Store in context for trace
                    context.putSemantic("searchResults", searchResult.getData());
                }
                yield searchResult;
            }
            case MODEL_ANALYSIS -> modelAnalysisAgent.execute(llmClient, history, user);
            case STATISTICS -> statisticsAgent.execute(llmClient, history);
            case VISUALIZATION -> {
                ChatResponse vizResult = conversationalAgent.execute(llmClient, history);
                // Try to extract visualization actions from the LLM reply
                if (vizResult.getReply() != null) {
                    List<VisualizationAction> suggested = parseVisualizationFromReply(vizResult.getReply());
                    if (!suggested.isEmpty()) {
                        vizResult.getActions().addAll(suggested);
                    }
                }
                yield vizResult;
            }
            case GENERAL -> conversationalAgent.execute(llmClient, history);
            default -> conversationalAgent.execute(llmClient, history);
        };
    }

    /**
     * Parse common location references from the LLM reply and generate FLY_TO actions.
     */
    private List<VisualizationAction> parseVisualizationFromReply(String reply) {
        List<VisualizationAction> actions = new java.util.ArrayList<>();
        // Extract coordinates if the LLM mentions them (e.g., "经度116.4, 纬度39.9")
        java.util.regex.Pattern coordPattern = java.util.regex.Pattern.compile(
                "(?:经度|lon(?:gitude)?)[:\\s]*([\\d.]+)[,，\\s]+(?:纬度|lat(?:itude)?)[:\\s]*([\\d.]+)",
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = coordPattern.matcher(reply);
        if (matcher.find()) {
            try {
                double lon = Double.parseDouble(matcher.group(1));
                double lat = Double.parseDouble(matcher.group(2));
                if (lon >= -180 && lon <= 180 && lat >= -90 && lat <= 90) {
                    actions.add(VisualizationAction.flyTo(lon, lat));
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return actions;
    }
}
