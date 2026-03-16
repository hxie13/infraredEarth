package cn.ac.sitp.infrared.ai.agent;

import cn.ac.sitp.infrared.ai.llm.LlmClient;
import cn.ac.sitp.infrared.ai.llm.LlmResponse;
import cn.ac.sitp.infrared.ai.model.*;
import cn.ac.sitp.infrared.service.NCService;
import cn.ac.sitp.infrared.service.NaturalDisasterService;
import cn.ac.sitp.infrared.util.RequestValueUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Agent for satellite data search and natural disaster queries.
 * Maps natural language to existing NCService and NaturalDisasterService calls.
 */
@Component
public class DataSearchAgent {

    private static final Logger log = LoggerFactory.getLogger(DataSearchAgent.class);

    @Autowired
    private NCService ncService;

    @Autowired
    private NaturalDisasterService naturalDisasterService;

    private static final String SYSTEM_PROMPT = """
            你是红外地球遥感平台的数据搜索助手。帮助用户搜索卫星遥感数据和自然灾害事件。

            日期格式: yyyy-MM-dd
            如果用户没有指定日期范围，默认使用最近1年。
            如果用户没有指定分页，默认 currPage=1, pageSize=10。

            搜索到结果后，用中文自然语言总结发现，包括数据条数和关键信息。
            如果搜索结果包含地理位置信息，建议用户可以在地图上查看。
            """;

    private List<ToolDefinition> getToolDefinitions() {
        List<ToolDefinition> tools = new ArrayList<>();

        // search_nc_data
        JSONObject ncParams = new JSONObject();
        ncParams.put("type", "object");
        JSONObject ncProps = new JSONObject();
        ncProps.put("satellite_type", prop("string", "卫星类型，如 Landsat系列, Sentinel-1, MODIS 等"));
        ncProps.put("begin_date", prop("string", "开始日期 yyyy-MM-dd"));
        ncProps.put("end_date", prop("string", "结束日期 yyyy-MM-dd"));
        ncProps.put("region_name", prop("string", "区域名称"));
        ncProps.put("img_type", prop("string", "影像类型"));
        ncProps.put("process_type", prop("string", "处理级别"));
        ncProps.put("resolution", prop("string", "空间分辨率"));
        ncProps.put("name", prop("string", "数据文件名关键词"));
        ncProps.put("band_number", prop("integer", "波段数"));
        ncParams.put("properties", ncProps);
        tools.add(new ToolDefinition("search_nc_data", "搜索卫星遥感NC数据", ncParams));

        // get_nc_types
        JSONObject emptyParams = new JSONObject();
        emptyParams.put("type", "object");
        emptyParams.put("properties", new JSONObject());
        tools.add(new ToolDefinition("get_nc_types", "获取可用的卫星数据类型和筛选选项", emptyParams));

        // search_disasters
        JSONObject disasterParams = new JSONObject();
        disasterParams.put("type", "object");
        JSONObject disasterProps = new JSONObject();
        disasterProps.put("country", prop("string", "国家名称"));
        disasterProps.put("place", prop("string", "地点名称"));
        disasterProps.put("type", prop("string", "灾害类型，如 地震、洪水、台风、火灾 等"));
        disasterProps.put("begin_date", prop("string", "开始日期 yyyy-MM-dd"));
        disasterProps.put("end_date", prop("string", "结束日期 yyyy-MM-dd"));
        disasterParams.put("properties", disasterProps);
        tools.add(new ToolDefinition("search_disasters", "搜索自然灾害事件记录", disasterParams));

        // get_disaster_types
        tools.add(new ToolDefinition("get_disaster_types", "获取可用的灾害类型列表", emptyParams));

        return tools;
    }

    /**
     * Execute a data search based on conversation context using LLM tool calling.
     */
    public ChatResponse execute(LlmClient llmClient, List<ChatMessage> history) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(SYSTEM_PROMPT));

        // Add conversation history
        int start = Math.max(0, history.size() - 10);
        for (int i = start; i < history.size(); i++) {
            messages.add(history.get(i));
        }

        List<ToolDefinition> tools = getToolDefinitions();

        // First LLM call: get tool call decision
        LlmResponse response = llmClient.chat(messages, tools);

        if (!response.hasToolCalls()) {
            return ChatResponse.withReply(response.getContent());
        }

        // Execute tool calls
        ChatResponse chatResponse = new ChatResponse();
        messages.add(ChatMessage.assistantWithToolCalls(response.getToolCalls()));

        for (ToolCall toolCall : response.getToolCalls()) {
            String toolName = toolCall.getFunction().getName();
            String argsJson = toolCall.getFunction().getArguments();
            Map<String, Object> result = executeTool(toolName, argsJson);

            String resultJson = JSON.toJSONString(result);
            messages.add(ChatMessage.toolResult(toolCall.getId(), toolName, resultJson));

            // Store data in response for frontend
            chatResponse.setData(result);
        }

        // Second LLM call: summarize results
        LlmResponse summary = llmClient.chat(messages, null);
        chatResponse.setReply(summary.getContent());

        return chatResponse;
    }

    /**
     * Execute a single tool by name (used by PlanExecutor).
     */
    public Map<String, Object> executeTool(String toolName, String argsJson) {
        JSONObject args = argsJson != null && !argsJson.isBlank()
                ? JSON.parseObject(argsJson) : new JSONObject();

        return switch (toolName) {
            case "search_nc_data" -> executeSearchNcData(args);
            case "get_nc_types" -> ncService.getNCTypeList();
            case "search_disasters" -> executeSearchDisasters(args);
            case "get_disaster_types" -> naturalDisasterService.getNaturalDisasterTypeList();
            default -> Map.of("error", "Unknown tool: " + toolName);
        };
    }

    private Map<String, Object> executeSearchNcData(JSONObject args) {
        int currPage = args.getIntValue("curr_page", 1);
        int pageSize = args.getIntValue("page_size", 10);
        Date beginDate = RequestValueUtils.parseBeginDate(args.getString("begin_date"));
        Date endDate = RequestValueUtils.parseEndDate(args.getString("end_date"));

        return ncService.getNCList(
                currPage, pageSize,
                trimNull(args.getString("name")),
                trimNull(args.getString("title")),
                args.getInteger("band_number"),
                trimNull(args.getString("region_name")),
                trimNull(args.getString("satellite_type")),
                trimNull(args.getString("resolution")),
                trimNull(args.getString("img_type")),
                trimNull(args.getString("process_type")),
                beginDate, endDate
        );
    }

    private Map<String, Object> executeSearchDisasters(JSONObject args) {
        int currPage = args.getIntValue("curr_page", 1);
        int pageSize = args.getIntValue("page_size", 10);
        Date beginDate = RequestValueUtils.parseBeginDate(args.getString("begin_date"));
        Date endDate = RequestValueUtils.parseEndDate(args.getString("end_date"));

        return naturalDisasterService.getNaturalDisasterList(
                currPage, pageSize,
                beginDate, endDate,
                trimNull(args.getString("country")),
                trimNull(args.getString("place")),
                trimNull(args.getString("type"))
        );
    }

    private static String trimNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static JSONObject prop(String type, String description) {
        JSONObject p = new JSONObject();
        p.put("type", type);
        p.put("description", description);
        return p;
    }
}
