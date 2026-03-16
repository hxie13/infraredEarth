package cn.ac.sitp.infrared.ai.model;

import com.alibaba.fastjson2.JSONObject;

/**
 * Represents an LLM tool/function call request.
 */
public class ToolCall {

    private String id;
    private String type = "function";
    private FunctionCall function;

    public ToolCall() {}

    public ToolCall(String id, String name, String arguments) {
        this.id = id;
        this.function = new FunctionCall(name, arguments);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("type", type);
        JSONObject fn = new JSONObject();
        fn.put("name", function.getName());
        fn.put("arguments", function.getArguments());
        json.put("function", fn);
        return json;
    }

    public static ToolCall fromJson(JSONObject json) {
        ToolCall tc = new ToolCall();
        tc.setId(json.getString("id"));
        tc.setType(json.getString("type"));
        JSONObject fn = json.getJSONObject("function");
        if (fn != null) {
            tc.setFunction(new FunctionCall(fn.getString("name"), fn.getString("arguments")));
        }
        return tc;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public FunctionCall getFunction() { return function; }
    public void setFunction(FunctionCall function) { this.function = function; }

    public static class FunctionCall {
        private String name;
        private String arguments; // JSON string

        public FunctionCall() {}

        public FunctionCall(String name, String arguments) {
            this.name = name;
            this.arguments = arguments;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getArguments() { return arguments; }
        public void setArguments(String arguments) { this.arguments = arguments; }
    }
}
