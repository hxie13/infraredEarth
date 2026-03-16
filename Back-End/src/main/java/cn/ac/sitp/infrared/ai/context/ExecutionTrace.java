package cn.ac.sitp.infrared.ai.context;

import cn.ac.sitp.infrared.ai.agent.AgentIntent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Records the full execution trace of an AI task for auditing and debugging.
 * Captures: user query → intent classification → plan → each step's input/output/duration.
 */
public class ExecutionTrace {

    private final String traceId;
    private String userQuery;
    private AgentIntent classifiedIntent;
    private String planId;
    private final List<StepTrace> steps = new ArrayList<>();
    private final Instant startTime;
    private Instant endTime;
    private long totalDurationMs;
    private String status; // "SUCCESS", "PARTIAL", "FAILED"

    public ExecutionTrace(String traceId) {
        this.traceId = traceId;
        this.startTime = Instant.now();
    }

    /**
     * Add a step trace record.
     */
    public void addStep(StepTrace step) {
        steps.add(step);
    }

    /**
     * Mark the trace as complete and calculate total duration.
     */
    public void complete(String status) {
        this.endTime = Instant.now();
        this.totalDurationMs = endTime.toEpochMilli() - startTime.toEpochMilli();
        this.status = status;
    }

    /**
     * Build a human-readable summary for logging.
     */
    public String toSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Trace[").append(traceId).append("] ");
        sb.append("intent=").append(classifiedIntent).append(" ");
        sb.append("steps=").append(steps.size()).append(" ");
        sb.append("status=").append(status).append(" ");
        sb.append("duration=").append(totalDurationMs).append("ms\n");
        for (StepTrace step : steps) {
            sb.append("  Step ").append(step.getStep())
                    .append(": ").append(step.getAgent()).append(".").append(step.getTool())
                    .append(" [").append(step.getStatus()).append("] ")
                    .append(step.getDurationMs()).append("ms\n");
        }
        return sb.toString();
    }

    // Getters / Setters
    public String getTraceId() { return traceId; }
    public String getUserQuery() { return userQuery; }
    public void setUserQuery(String userQuery) { this.userQuery = userQuery; }
    public AgentIntent getClassifiedIntent() { return classifiedIntent; }
    public void setClassifiedIntent(AgentIntent classifiedIntent) { this.classifiedIntent = classifiedIntent; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public List<StepTrace> getSteps() { return Collections.unmodifiableList(steps); }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public long getTotalDurationMs() { return totalDurationMs; }
    public String getStatus() { return status; }
}
