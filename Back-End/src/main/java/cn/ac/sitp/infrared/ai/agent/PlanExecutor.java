package cn.ac.sitp.infrared.ai.agent;

import cn.ac.sitp.infrared.ai.context.ExecutionTrace;
import cn.ac.sitp.infrared.ai.context.StepTrace;
import cn.ac.sitp.infrared.ai.context.TaskContext;
import cn.ac.sitp.infrared.ai.model.*;
import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Executes an ExecutionPlan step by step using TaskContext for state management
 * and ExecutionTrace for auditing.
 */
@Component
public class PlanExecutor {

    private static final Logger log = LoggerFactory.getLogger(PlanExecutor.class);

    @Autowired
    private DataSearchAgent dataSearchAgent;

    @Autowired
    private ModelAnalysisAgent modelAnalysisAgent;

    @Autowired
    private VisualizationAgent visualizationAgent;

    @Autowired
    private StatisticsAgent statisticsAgent;

    /**
     * Execute all steps in the plan sequentially using TaskContext.
     */
    public ChatResponse execute(ExecutionPlan plan, AxrrAccount user, TaskContext context) {
        context.setPlan(plan);
        ExecutionTrace trace = context.getTrace();

        ChatResponse finalResponse = new ChatResponse();
        List<String> summaries = new ArrayList<>();
        List<VisualizationAction> allActions = new ArrayList<>();

        int successCount = 0;
        int failCount = 0;

        for (PlanStep step : plan.getSteps()) {
            log.info("Executing plan step {}: {}.{}", step.getStep(), step.getAgent(), step.getTool());

            StepTrace stepTrace = new StepTrace(step.getStep(), step.getAgent(), step.getTool());

            try {
                // Resolve $step_N references in args
                JSONObject resolvedArgs = resolveReferences(step.getArgs(), context);
                String argsJson = resolvedArgs.toJSONString();
                stepTrace.setInputArgs(argsJson);

                Object result = executeStep(step.getAgent(), step.getTool(), argsJson, user, context);
                context.putStepResult(step.getStep(), result);

                // Store semantic results for cross-step reference
                storeSemanticResults(step, result, context);

                // Collect visualization actions from VisualizationAgent results
                if (result instanceof ChatResponse cr) {
                    if (cr.getActions() != null) {
                        allActions.addAll(cr.getActions());
                    }
                    if (cr.getReply() != null) {
                        summaries.add(cr.getReply());
                    }
                    if (cr.getData() != null) {
                        finalResponse.setData(cr.getData());
                    }
                    stepTrace.success(cr.getReply() != null ? cr.getReply() : "ok");
                } else if (result instanceof Map<?, ?> mapResult) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) mapResult;
                    finalResponse.setData(data);

                    // Auto-generate visualization suggestions for data results
                    List<VisualizationAction> suggested = visualizationAgent.suggestActionsForData(data);
                    allActions.addAll(suggested);

                    String resultSummary = summarizeResult(data);
                    stepTrace.success(resultSummary);
                } else {
                    stepTrace.success("completed");
                }

                successCount++;

            } catch (Exception e) {
                log.error("Plan step {} failed: {}.{}", step.getStep(), step.getAgent(), step.getTool(), e);
                summaries.add("步骤 " + step.getStep() + " 执行失败: " + e.getMessage());
                stepTrace.fail(e.getMessage());
                failCount++;
            }

            trace.addStep(stepTrace);
        }

        // Determine trace status
        String traceStatus = failCount == 0 ? "SUCCESS" : (successCount > 0 ? "PARTIAL" : "FAILED");
        trace.complete(traceStatus);

        finalResponse.setActions(allActions);
        if (!summaries.isEmpty()) {
            finalResponse.setReply(String.join("\n", summaries));
        }

        return finalResponse;
    }

    /**
     * Legacy method for backward compatibility (creates a default TaskContext).
     */
    public ChatResponse execute(ExecutionPlan plan, AxrrAccount user) {
        TaskContext context = new TaskContext();
        return execute(plan, user, context);
    }

    private Object executeStep(String agent, String tool, String argsJson, AxrrAccount user,
                               TaskContext context) {
        Map<String, Object> contextMap = new HashMap<>();
        Object lastResult = context.getLastStepResult();
        if (lastResult != null) {
            contextMap.put("lastResult", lastResult);
        }

        return switch (agent) {
            case "DataSearchAgent" -> dataSearchAgent.executeTool(tool, argsJson);
            case "ModelAnalysisAgent" -> {
                if (user == null) {
                    yield Map.of("error", "此操作需要登录");
                }
                yield modelAnalysisAgent.executeTool(tool, argsJson, user);
            }
            case "VisualizationAgent" -> visualizationAgent.executeTool(tool, argsJson, contextMap);
            case "StatisticsAgent" -> statisticsAgent.executeTool(tool, argsJson);
            default -> Map.of("error", "Unknown agent: " + agent);
        };
    }

    /**
     * Store semantic results for more robust cross-step data passing.
     */
    @SuppressWarnings("unchecked")
    private void storeSemanticResults(PlanStep step, Object result, TaskContext context) {
        if (result instanceof Map<?, ?> mapResult) {
            Map<String, Object> data = (Map<String, Object>) mapResult;

            switch (step.getTool()) {
                case "search_nc_data" -> {
                    context.putSemantic("searchResults", data);
                    // Extract NC IDs for downstream tools
                    String ncIds = extractNcIds(data);
                    if (ncIds != null) {
                        context.putSemantic("ncIds", ncIds);
                    }
                }
                case "search_disasters" -> context.putSemantic("disasterResults", data);
                case "create_dataset" -> context.putSemantic("datasetResult", data);
                case "submit_job" -> context.putSemantic("jobResult", data);
                case "list_algorithms" -> context.putSemantic("algorithms", data);
                case "zonal_stats", "area_statistics", "time_series_stats" ->
                        context.putSemantic("statistics", data);
            }
        }
    }

    /**
     * Resolve $step_N references in args to actual values from previous steps.
     */
    private JSONObject resolveReferences(JSONObject args, TaskContext context) {
        if (args == null) return new JSONObject();

        JSONObject resolved = new JSONObject();
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String strVal && strVal.startsWith("$step_")) {
                try {
                    int refStep = Integer.parseInt(strVal.substring(6));
                    Object refResult = context.getStepResult(refStep);
                    if (refResult != null) {
                        resolved.put(entry.getKey(), extractUsableValue(refResult));
                    } else {
                        resolved.put(entry.getKey(), value);
                    }
                } catch (NumberFormatException e) {
                    resolved.put(entry.getKey(), value);
                }
            } else if (value instanceof String strVal && strVal.startsWith("$context.")) {
                // Semantic context reference: $context.ncIds, $context.datasetId, etc.
                String key = strVal.substring(9);
                Object contextVal = context.getSemantic(key);
                resolved.put(entry.getKey(), contextVal != null ? contextVal : value);
            } else {
                resolved.put(entry.getKey(), value);
            }
        }
        return resolved;
    }

    /**
     * Extract a usable value from a step result for passing to the next step.
     */
    @SuppressWarnings("unchecked")
    private Object extractUsableValue(Object result) {
        if (result instanceof Map<?, ?> mapResult) {
            Map<String, Object> data = (Map<String, Object>) mapResult;

            // Extract NC IDs from search results
            String ncIds = extractNcIds(data);
            if (ncIds != null) return ncIds;

            // Extract dataset creation result
            if (data.containsKey("success") && data.containsKey("nc_count")) {
                return JSON.toJSONString(data);
            }
        }
        return JSON.toJSONString(result);
    }

    /**
     * Extract NC IDs as comma-separated string from search results.
     */
    @SuppressWarnings("unchecked")
    private String extractNcIds(Map<String, Object> data) {
        Object list = data.get("ncList");
        if (list == null) list = data.get("list");
        if (list instanceof List<?> items && !items.isEmpty()) {
            if (items.getFirst() instanceof Map<?, ?> firstItem) {
                Map<String, Object> item = (Map<String, Object>) firstItem;
                if (item.containsKey("id")) {
                    StringBuilder ids = new StringBuilder();
                    for (Object obj : items) {
                        if (obj instanceof Map<?, ?> m) {
                            Object id = ((Map<String, Object>) m).get("id");
                            if (id != null) {
                                if (!ids.isEmpty()) ids.append(",");
                                ids.append(id);
                            }
                        }
                    }
                    return ids.toString();
                }
            }
        }
        return null;
    }

    /**
     * Create a brief summary of a result map for trace logging.
     */
    private String summarizeResult(Map<String, Object> data) {
        if (data.containsKey("error")) return "error: " + data.get("error");
        if (data.containsKey("ncList")) return "ncList: " + ((List<?>) data.get("ncList")).size() + " items";
        if (data.containsKey("naturalDisasterList"))
            return "disasters: " + ((List<?>) data.get("naturalDisasterList")).size() + " items";
        if (data.containsKey("statistics")) return "statistics computed";
        if (data.containsKey("areaStats")) return "area statistics computed";
        if (data.containsKey("timeSeries")) return "time series computed";
        if (data.containsKey("success")) return "success";
        return "data keys: " + data.keySet();
    }
}
