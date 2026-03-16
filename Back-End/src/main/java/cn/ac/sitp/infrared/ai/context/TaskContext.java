package cn.ac.sitp.infrared.ai.context;

import cn.ac.sitp.infrared.ai.agent.AgentIntent;
import cn.ac.sitp.infrared.ai.model.ExecutionPlan;

import java.util.*;

/**
 * Unified task context that flows through the entire execution pipeline.
 * Replaces the simple Map<Integer, Object> stepResults with semantically rich context.
 */
public class TaskContext {

    private final String taskId;
    private String userQuery;
    private AgentIntent intent;
    private String aoi;                                // Area of Interest (region name or GeoJSON)
    private String timeRange;                          // e.g. "2025-01-01 to 2025-12-31"
    private final List<String> datasetIds = new ArrayList<>();   // NC IDs involved
    private final Map<Integer, Object> stepResults = new LinkedHashMap<>();  // step# → result
    private final Map<String, Object> semanticResults = new LinkedHashMap<>(); // semantic key → value
    private Object finalResult;
    private ExecutionPlan plan;
    private final ExecutionTrace trace;

    public TaskContext() {
        this.taskId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        this.trace = new ExecutionTrace(taskId);
    }

    public TaskContext(String userQuery, AgentIntent intent) {
        this();
        this.userQuery = userQuery;
        this.intent = intent;
        this.trace.setUserQuery(userQuery);
        this.trace.setClassifiedIntent(intent);
    }

    // ── Step result management ──────────────────────────────────────────

    /**
     * Store result of a step by step number (for $step_N references).
     */
    public void putStepResult(int step, Object result) {
        stepResults.put(step, result);
    }

    public Object getStepResult(int step) {
        return stepResults.get(step);
    }

    public Map<Integer, Object> getAllStepResults() {
        return Collections.unmodifiableMap(stepResults);
    }

    /**
     * Get the last step result.
     */
    public Object getLastStepResult() {
        return stepResults.values().stream().reduce((a, b) -> b).orElse(null);
    }

    // ── Semantic result management ──────────────────────────────────────

    /**
     * Store a result by semantic key (e.g. "searchResults", "datasetId", "jobId").
     * More robust than step number references for cross-step data passing.
     */
    public void putSemantic(String key, Object value) {
        semanticResults.put(key, value);
    }

    public Object getSemantic(String key) {
        return semanticResults.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getSemantic(String key, Class<T> type) {
        Object val = semanticResults.get(key);
        return type.isInstance(val) ? (T) val : null;
    }

    // ── Dataset management ──────────────────────────────────────────────

    public void addDatasetIds(Collection<String> ids) {
        datasetIds.addAll(ids);
    }

    public void addDatasetId(String id) {
        datasetIds.add(id);
    }

    // ── Getters / Setters ───────────────────────────────────────────────

    public String getTaskId() { return taskId; }
    public String getUserQuery() { return userQuery; }
    public void setUserQuery(String userQuery) { this.userQuery = userQuery; }
    public AgentIntent getIntent() { return intent; }
    public void setIntent(AgentIntent intent) { this.intent = intent; }
    public String getAoi() { return aoi; }
    public void setAoi(String aoi) { this.aoi = aoi; }
    public String getTimeRange() { return timeRange; }
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }
    public List<String> getDatasetIds() { return datasetIds; }
    public Object getFinalResult() { return finalResult; }
    public void setFinalResult(Object finalResult) { this.finalResult = finalResult; }
    public ExecutionPlan getPlan() { return plan; }
    public void setPlan(ExecutionPlan plan) {
        this.plan = plan;
        this.trace.setPlanId(plan != null ? plan.getPlanId() : null);
    }
    public ExecutionTrace getTrace() { return trace; }
}
