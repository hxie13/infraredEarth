package cn.ac.sitp.infrared.ai.service;

import cn.ac.sitp.infrared.ai.context.ExecutionTrace;
import cn.ac.sitp.infrared.ai.context.StepTrace;
import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Persists ExecutionTrace records to the database for auditing and debugging.
 * Uses async writes to avoid blocking the chat response.
 */
@Service
public class ExecutionTraceService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionTraceService.class);

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    /**
     * Save an execution trace asynchronously.
     */
    @Async
    public void saveTrace(ExecutionTrace trace, Long userId) {
        if (trace == null) return;

        // Always log the summary
        log.info("ExecutionTrace:\n{}", trace.toSummary());

        // Persist to database if available
        if (jdbcTemplate == null) {
            log.debug("JdbcTemplate not available, skipping trace persistence");
            return;
        }

        try {
            String stepsJson = JSON.toJSONString(trace.getSteps().stream().map(s -> {
                var m = new java.util.LinkedHashMap<String, Object>();
                m.put("step", s.getStep());
                m.put("agent", s.getAgent());
                m.put("tool", s.getTool());
                m.put("status", s.getStatus());
                m.put("durationMs", s.getDurationMs());
                m.put("inputArgs", s.getInputArgs());
                m.put("outputSummary", s.getOutputSummary());
                m.put("errorMessage", s.getErrorMessage());
                return m;
            }).toList());

            jdbcTemplate.update(
                    """
                    INSERT INTO ai_execution_trace
                    (trace_id, user_id, user_query, classified_intent, plan_id, steps_json,
                     status, total_duration_ms, start_time, end_time)
                    VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?)
                    """,
                    trace.getTraceId(),
                    userId,
                    truncate(trace.getUserQuery(), 2000),
                    trace.getClassifiedIntent() != null ? trace.getClassifiedIntent().name() : null,
                    trace.getPlanId(),
                    stepsJson,
                    trace.getStatus(),
                    trace.getTotalDurationMs(),
                    toTimestamp(trace.getStartTime()),
                    toTimestamp(trace.getEndTime())
            );

            log.debug("Execution trace {} persisted to database", trace.getTraceId());
        } catch (Exception e) {
            // Don't fail the response if trace persistence fails
            log.warn("Failed to persist execution trace {}: {}", trace.getTraceId(), e.getMessage());
        }
    }

    private static Timestamp toTimestamp(Instant instant) {
        return instant != null ? Timestamp.from(instant) : null;
    }

    private static String truncate(String s, int maxLen) {
        return s != null && s.length() > maxLen ? s.substring(0, maxLen) : s;
    }
}
