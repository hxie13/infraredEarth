package cn.ac.sitp.infrared.ai.model;

import com.alibaba.fastjson2.JSONObject;

/**
 * A single step in an ExecutionPlan.
 */
public class PlanStep {

    private int step;
    private String agent;   // "DataSearchAgent", "ModelAnalysisAgent", "VisualizationAgent"
    private String tool;    // tool name, e.g. "search_nc_data"
    private JSONObject args = new JSONObject();

    public PlanStep() {}

    public PlanStep(String agent, String tool) {
        this.agent = agent;
        this.tool = tool;
    }

    public PlanStep(String agent, String tool, JSONObject args) {
        this.agent = agent;
        this.tool = tool;
        this.args = args != null ? args : new JSONObject();
    }

    public int getStep() { return step; }
    public void setStep(int step) { this.step = step; }
    public String getAgent() { return agent; }
    public void setAgent(String agent) { this.agent = agent; }
    public String getTool() { return tool; }
    public void setTool(String tool) { this.tool = tool; }
    public JSONObject getArgs() { return args; }
    public void setArgs(JSONObject args) { this.args = args; }
}
