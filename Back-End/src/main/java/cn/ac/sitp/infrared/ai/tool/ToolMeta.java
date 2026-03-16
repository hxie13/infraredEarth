package cn.ac.sitp.infrared.ai.tool;

import cn.ac.sitp.infrared.ai.model.ToolDefinition;
import com.alibaba.fastjson2.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Rich metadata for a tool/function available in the agent system.
 * Goes beyond ToolDefinition by including usage guidance, examples, and routing info.
 */
public class ToolMeta {

    private final String name;
    private final String description;
    private final String agent;              // owning agent: "DataSearchAgent", "ModelAnalysisAgent", etc.
    private final List<String> whenToUse;    // scenarios where this tool is appropriate
    private final List<String> whenNotToUse; // scenarios where this tool should NOT be used
    private final JSONObject inputSchema;    // JSON Schema for input parameters
    private final JSONObject outputSchema;   // description of output structure
    private final List<ToolExample> examples;

    public ToolMeta(String name, String description, String agent,
                    List<String> whenToUse, List<String> whenNotToUse,
                    JSONObject inputSchema, JSONObject outputSchema,
                    List<ToolExample> examples) {
        this.name = name;
        this.description = description;
        this.agent = agent;
        this.whenToUse = whenToUse != null ? whenToUse : List.of();
        this.whenNotToUse = whenNotToUse != null ? whenNotToUse : List.of();
        this.inputSchema = inputSchema != null ? inputSchema : new JSONObject();
        this.outputSchema = outputSchema != null ? outputSchema : new JSONObject();
        this.examples = examples != null ? examples : List.of();
    }

    /**
     * Convert to ToolDefinition for LLM function-calling API.
     */
    public ToolDefinition toToolDefinition() {
        return new ToolDefinition(name, description, inputSchema);
    }

    /**
     * Build a rich text prompt fragment describing this tool for planning agents.
     */
    public String toPlannerPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("- **").append(name).append("**: ").append(description).append("\n");
        if (!whenToUse.isEmpty()) {
            sb.append("  适用: ").append(String.join(", ", whenToUse)).append("\n");
        }
        if (!whenNotToUse.isEmpty()) {
            sb.append("  不适用: ").append(String.join(", ", whenNotToUse)).append("\n");
        }
        if (inputSchema.containsKey("properties")) {
            JSONObject props = inputSchema.getJSONObject("properties");
            if (props != null && !props.isEmpty()) {
                sb.append("  参数: ");
                sb.append(String.join(", ", props.keySet()));
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAgent() { return agent; }
    public List<String> getWhenToUse() { return whenToUse; }
    public List<String> getWhenNotToUse() { return whenNotToUse; }
    public JSONObject getInputSchema() { return inputSchema; }
    public JSONObject getOutputSchema() { return outputSchema; }
    public List<ToolExample> getExamples() { return examples; }

    /**
     * Example usage of a tool (input → output).
     */
    public record ToolExample(String description, Map<String, Object> input, Map<String, Object> output) {}

    /**
     * Builder for fluent construction.
     */
    public static Builder builder(String name, String agent) {
        return new Builder(name, agent);
    }

    public static class Builder {
        private final String name;
        private final String agent;
        private String description = "";
        private List<String> whenToUse = List.of();
        private List<String> whenNotToUse = List.of();
        private JSONObject inputSchema = new JSONObject();
        private JSONObject outputSchema = new JSONObject();
        private List<ToolExample> examples = List.of();

        Builder(String name, String agent) {
            this.name = name;
            this.agent = agent;
        }

        public Builder description(String d) { this.description = d; return this; }
        public Builder whenToUse(String... s) { this.whenToUse = List.of(s); return this; }
        public Builder whenNotToUse(String... s) { this.whenNotToUse = List.of(s); return this; }
        public Builder inputSchema(JSONObject s) { this.inputSchema = s; return this; }
        public Builder outputSchema(JSONObject s) { this.outputSchema = s; return this; }
        public Builder examples(List<ToolExample> e) { this.examples = e; return this; }

        public ToolMeta build() {
            return new ToolMeta(name, description, agent, whenToUse, whenNotToUse,
                    inputSchema, outputSchema, examples);
        }
    }
}
