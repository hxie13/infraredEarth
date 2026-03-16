package cn.ac.sitp.infrared.ai.context;

import java.time.Instant;

/**
 * Trace record for a single execution step.
 */
public class StepTrace {

    private final int step;
    private final String agent;
    private final String tool;
    private String inputArgs;
    private String outputSummary;
    private long durationMs;
    private String status; // "SUCCESS", "FAILED", "SKIPPED"
    private String errorMessage;
    private final Instant startTime;
    private Instant endTime;

    public StepTrace(int step, String agent, String tool) {
        this.step = step;
        this.agent = agent;
        this.tool = tool;
        this.startTime = Instant.now();
    }

    /**
     * Mark step as successfully completed.
     */
    public void success(String outputSummary) {
        this.endTime = Instant.now();
        this.durationMs = endTime.toEpochMilli() - startTime.toEpochMilli();
        this.outputSummary = outputSummary;
        this.status = "SUCCESS";
    }

    /**
     * Mark step as failed.
     */
    public void fail(String errorMessage) {
        this.endTime = Instant.now();
        this.durationMs = endTime.toEpochMilli() - startTime.toEpochMilli();
        this.errorMessage = errorMessage;
        this.status = "FAILED";
    }

    // Getters
    public int getStep() { return step; }
    public String getAgent() { return agent; }
    public String getTool() { return tool; }
    public String getInputArgs() { return inputArgs; }
    public void setInputArgs(String inputArgs) { this.inputArgs = inputArgs; }
    public String getOutputSummary() { return outputSummary; }
    public long getDurationMs() { return durationMs; }
    public String getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
}
