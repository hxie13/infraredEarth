package cn.ac.sitp.infrared.ai.agent;

import cn.ac.sitp.infrared.ai.llm.LlmClient;
import cn.ac.sitp.infrared.ai.llm.LlmResponse;
import cn.ac.sitp.infrared.ai.model.*;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Agent for statistical analysis of algorithm outputs and data results.
 * Provides zonal statistics, area calculations, and time-series analysis.
 */
@Component
public class StatisticsAgent {

    private static final Logger log = LoggerFactory.getLogger(StatisticsAgent.class);

    private static final String SYSTEM_PROMPT = """
            你是红外地球遥感平台的统计分析助手。帮助用户对遥感分析结果进行区域统计、面积统计和时序分析。

            可用工具：
            1. zonal_stats: 对分析结果进行区域统计（均值、最大值、最小值、面积等）
            2. area_statistics: 按类别统计面积占比（适用于分类结果）
            3. time_series_stats: 多时相数据的时序统计分析

            注意：统计分析通常在算法处理完成后执行。如果还没有分析结果，建议用户先提交分析任务。
            """;

    /**
     * Execute statistics via LLM tool-calling flow.
     */
    public ChatResponse execute(LlmClient llmClient, List<ChatMessage> history) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(SYSTEM_PROMPT));

        int start = Math.max(0, history.size() - 10);
        for (int i = start; i < history.size(); i++) {
            messages.add(history.get(i));
        }

        List<ToolDefinition> tools = getToolDefinitions();
        LlmResponse response = llmClient.chat(messages, tools);

        if (!response.hasToolCalls()) {
            return ChatResponse.withReply(response.getContent());
        }

        ChatResponse chatResponse = new ChatResponse();
        messages.add(ChatMessage.assistantWithToolCalls(response.getToolCalls()));

        for (ToolCall toolCall : response.getToolCalls()) {
            String toolName = toolCall.getFunction().getName();
            String argsJson = toolCall.getFunction().getArguments();
            Map<String, Object> result = executeTool(toolName, argsJson);

            String resultJson = JSON.toJSONString(result);
            messages.add(ChatMessage.toolResult(toolCall.getId(), toolName, resultJson));
            chatResponse.setData(result);
        }

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
            case "zonal_stats" -> executeZonalStats(args);
            case "area_statistics" -> executeAreaStatistics(args);
            case "time_series_stats" -> executeTimeSeriesStats(args);
            default -> Map.of("error", "Unknown tool: " + toolName);
        };
    }

    /**
     * Zonal statistics: compute mean/max/min/sum for a region.
     * Currently returns simulated results — will connect to actual computation engine later.
     */
    private Map<String, Object> executeZonalStats(JSONObject args) {
        String resultId = args.getString("result_id");
        String statType = args.getString("stat_type");
        String aoi = args.getString("aoi");

        if (resultId == null || resultId.isBlank()) {
            return Map.of("error", "请提供分析结果ID (result_id)");
        }

        log.info("Computing zonal statistics: resultId={}, statType={}, aoi={}", resultId, statType, aoi);

        // Simulated results — in production, this would read the actual result raster
        // and compute real statistics using a GIS library (e.g., GeoTools)
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("result_id", resultId);
        stats.put("aoi", aoi != null ? aoi : "全区域");
        stats.put("stat_type", statType != null ? statType : "all");

        Random rng = new Random(resultId.hashCode());
        stats.put("mean", Math.round(rng.nextDouble() * 100.0) / 100.0);
        stats.put("max", Math.round(rng.nextDouble() * 200.0) / 100.0);
        stats.put("min", Math.round(rng.nextDouble() * 50.0) / 100.0);
        stats.put("std", Math.round(rng.nextDouble() * 30.0) / 100.0);
        stats.put("pixel_count", 10000 + rng.nextInt(90000));
        stats.put("valid_pixel_count", 9000 + rng.nextInt(1000));
        stats.put("area_km2", Math.round(rng.nextDouble() * 5000.0) / 10.0);
        stats.put("simulated", true);
        stats.put("message", "统计分析完成（模拟数据）。实际计算引擎接入后将返回真实统计结果。");

        return Map.of("statistics", stats);
    }

    /**
     * Area statistics: compute area by class/category.
     */
    private Map<String, Object> executeAreaStatistics(JSONObject args) {
        String resultId = args.getString("result_id");
        String classField = args.getString("class_field");

        if (resultId == null || resultId.isBlank()) {
            return Map.of("error", "请提供分析结果ID (result_id)");
        }

        log.info("Computing area statistics: resultId={}, classField={}", resultId, classField);

        // Simulated classification area results
        List<Map<String, Object>> classes = new ArrayList<>();
        String[] classNames = {"水体", "植被", "裸土", "建筑", "农田", "其他"};
        Random rng = new Random(resultId.hashCode());
        double totalArea = 1000 + rng.nextDouble() * 9000;
        double remaining = 100.0;

        for (int i = 0; i < classNames.length; i++) {
            double pct = i < classNames.length - 1
                    ? Math.round(rng.nextDouble() * remaining * 0.5 * 100) / 100.0
                    : Math.round(remaining * 100) / 100.0;
            remaining -= pct;
            Map<String, Object> cls = new LinkedHashMap<>();
            cls.put("class_name", classNames[i]);
            cls.put("class_id", i + 1);
            cls.put("area_km2", Math.round(totalArea * pct / 100 * 10) / 10.0);
            cls.put("percentage", pct);
            classes.add(cls);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("result_id", resultId);
        result.put("total_area_km2", Math.round(totalArea * 10) / 10.0);
        result.put("classes", classes);
        result.put("simulated", true);
        result.put("message", "面积统计完成（模拟数据）。");

        return Map.of("areaStats", result);
    }

    /**
     * Time-series statistics: compute trends across multiple dates.
     */
    private Map<String, Object> executeTimeSeriesStats(JSONObject args) {
        String ncIdsStr = args.getString("nc_ids");
        Integer band = args.getInteger("band");
        String aoi = args.getString("aoi");

        if (ncIdsStr == null || ncIdsStr.isBlank()) {
            return Map.of("error", "请提供多时相NC数据ID列表 (nc_ids)");
        }

        String[] ids = ncIdsStr.split(",");
        log.info("Computing time-series statistics: {} datasets, band={}, aoi={}",
                ids.length, band, aoi);

        // Simulated time-series results
        List<Map<String, Object>> points = new ArrayList<>();
        Random rng = new Random(ncIdsStr.hashCode());
        double baseVal = 0.3 + rng.nextDouble() * 0.3;

        for (int i = 0; i < ids.length; i++) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("nc_id", ids[i].trim());
            point.put("index", i + 1);
            double val = baseVal + Math.sin(i * 0.5) * 0.15 + (rng.nextDouble() - 0.5) * 0.05;
            point.put("mean_value", Math.round(val * 1000) / 1000.0);
            points.add(point);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("aoi", aoi != null ? aoi : "全区域");
        result.put("band", band != null ? band : 1);
        result.put("data_count", ids.length);
        result.put("time_series", points);

        // Trend summary
        double first = (double) points.getFirst().get("mean_value");
        double last = (double) points.getLast().get("mean_value");
        String trend = last > first + 0.05 ? "上升趋势" : (last < first - 0.05 ? "下降趋势" : "基本平稳");
        result.put("trend", trend);
        result.put("trend_slope", Math.round((last - first) / ids.length * 10000) / 10000.0);
        result.put("simulated", true);
        result.put("message", "时序统计分析完成（模拟数据）。");

        return Map.of("timeSeries", result);
    }

    private List<ToolDefinition> getToolDefinitions() {
        List<ToolDefinition> tools = new ArrayList<>();

        // zonal_stats
        JSONObject zonalParams = new JSONObject();
        zonalParams.put("type", "object");
        JSONObject zonalProps = new JSONObject();
        zonalProps.put("result_id", prop("string", "分析结果ID或NC数据ID"));
        zonalProps.put("stat_type", prop("string", "统计类型: mean, max, min, sum, std, histogram, all"));
        zonalProps.put("aoi", prop("string", "感兴趣区域名称或GeoJSON"));
        zonalParams.put("properties", zonalProps);
        zonalParams.put("required", List.of("result_id"));
        tools.add(new ToolDefinition("zonal_stats", "对分析结果进行区域统计", zonalParams));

        // area_statistics
        JSONObject areaParams = new JSONObject();
        areaParams.put("type", "object");
        JSONObject areaProps = new JSONObject();
        areaProps.put("result_id", prop("string", "分类结果ID"));
        areaProps.put("class_field", prop("string", "分类字段名（可选）"));
        areaParams.put("properties", areaProps);
        areaParams.put("required", List.of("result_id"));
        tools.add(new ToolDefinition("area_statistics", "按类别统计面积占比", areaParams));

        // time_series_stats
        JSONObject tsParams = new JSONObject();
        tsParams.put("type", "object");
        JSONObject tsProps = new JSONObject();
        tsProps.put("nc_ids", prop("string", "多时相NC数据ID列表，逗号分隔"));
        tsProps.put("band", prop("integer", "统计波段号"));
        tsProps.put("aoi", prop("string", "感兴趣区域名称或GeoJSON"));
        tsParams.put("properties", tsProps);
        tsParams.put("required", List.of("nc_ids"));
        tools.add(new ToolDefinition("time_series_stats", "多时相数据时序统计分析", tsParams));

        return tools;
    }

    private static JSONObject prop(String type, String description) {
        JSONObject p = new JSONObject();
        p.put("type", type);
        p.put("description", description);
        return p;
    }
}
