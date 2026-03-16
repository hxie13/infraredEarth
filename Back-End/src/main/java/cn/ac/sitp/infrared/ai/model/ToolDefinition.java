package cn.ac.sitp.infrared.ai.model;

import com.alibaba.fastjson2.JSONObject;

/**
 * Defines a tool/function that an agent can call (OpenAI function-calling schema).
 */
public class ToolDefinition {

    private String type = "function";
    private FunctionSchema function;

    public ToolDefinition(String name, String description, JSONObject parameters) {
        this.function = new FunctionSchema(name, description, parameters);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", type);
        JSONObject fn = new JSONObject();
        fn.put("name", function.getName());
        fn.put("description", function.getDescription());
        fn.put("parameters", function.getParameters());
        json.put("function", fn);
        return json;
    }

    public String getName() {
        return function != null ? function.getName() : null;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public FunctionSchema getFunction() { return function; }
    public void setFunction(FunctionSchema function) { this.function = function; }

    public static class FunctionSchema {
        private String name;
        private String description;
        private JSONObject parameters;

        public FunctionSchema(String name, String description, JSONObject parameters) {
            this.name = name;
            this.description = description;
            this.parameters = parameters;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public JSONObject getParameters() { return parameters; }
        public void setParameters(JSONObject parameters) { this.parameters = parameters; }
    }
}
