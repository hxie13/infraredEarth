package cn.ac.sitp.infrared.ai.workflow;

import java.util.List;
import java.util.Map;

/**
 * Pre-defined workflow template for common multi-step tasks.
 * Reduces LLM planning errors by providing reliable step sequences.
 */
public class WorkflowTemplate {

    private final String id;
    private final String name;
    private final String description;
    private final List<String> triggerKeywords;
    private final List<WorkflowStep> steps;

    public WorkflowTemplate(String id, String name, String description,
                            List<String> triggerKeywords, List<WorkflowStep> steps) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.triggerKeywords = triggerKeywords;
        this.steps = steps;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getTriggerKeywords() { return triggerKeywords; }
    public List<WorkflowStep> getSteps() { return steps; }

    /**
     * A single step in a workflow template.
     */
    public static class WorkflowStep {
        private final String agent;
        private final String tool;
        private final Map<String, String> argMapping; // key → value or "$step_N" reference

        public WorkflowStep(String agent, String tool) {
            this(agent, tool, Map.of());
        }

        public WorkflowStep(String agent, String tool, Map<String, String> argMapping) {
            this.agent = agent;
            this.tool = tool;
            this.argMapping = argMapping;
        }

        public String getAgent() { return agent; }
        public String getTool() { return tool; }
        public Map<String, String> getArgMapping() { return argMapping; }
    }
}
