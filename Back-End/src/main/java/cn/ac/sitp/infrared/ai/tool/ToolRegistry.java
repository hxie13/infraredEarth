package cn.ac.sitp.infrared.ai.tool;

import cn.ac.sitp.infrared.ai.model.ToolDefinition;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Central registry for all tools available in the multi-agent system.
 * Single source of truth — both TaskPlanningAgent and execution agents read from here.
 */
@Component
public class ToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(ToolRegistry.class);

    private final Map<String, ToolMeta> toolsByName = new LinkedHashMap<>();
    private final Map<String, List<ToolMeta>> toolsByAgent = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        registerDataSearchTools();
        registerModelAnalysisTools();
        registerVisualizationTools();
        registerStatisticsTools();
        log.info("ToolRegistry initialized: {} tools across {} agents",
                toolsByName.size(), toolsByAgent.size());
    }

    // ── Registration ────────────────────────────────────────────────────

    public void register(ToolMeta meta) {
        toolsByName.put(meta.getName(), meta);
        toolsByAgent.computeIfAbsent(meta.getAgent(), k -> new ArrayList<>()).add(meta);
    }

    // ── Queries ─────────────────────────────────────────────────────────

    public ToolMeta getTool(String name) {
        return toolsByName.get(name);
    }

    public List<ToolMeta> getToolsForAgent(String agent) {
        return toolsByAgent.getOrDefault(agent, List.of());
    }

    public List<ToolDefinition> getToolDefinitions(String agent) {
        return getToolsForAgent(agent).stream()
                .map(ToolMeta::toToolDefinition)
                .collect(Collectors.toList());
    }

    public Collection<ToolMeta> allTools() {
        return Collections.unmodifiableCollection(toolsByName.values());
    }

    public Set<String> allAgentNames() {
        return Collections.unmodifiableSet(toolsByAgent.keySet());
    }

    /**
     * Build a comprehensive catalog prompt for the TaskPlanningAgent.
     * Groups tools by agent with rich metadata (when_to_use, params).
     */
    public String buildPlannerCatalogPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("## 可用的 Agent 和工具\n\n");
        for (Map.Entry<String, List<ToolMeta>> entry : toolsByAgent.entrySet()) {
            sb.append("### ").append(entry.getKey()).append("\n");
            for (ToolMeta tool : entry.getValue()) {
                sb.append(tool.toPlannerPrompt());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // ── Tool Registrations ──────────────────────────────────────────────

    private void registerDataSearchTools() {
        // search_nc_data
        JSONObject ncParams = new JSONObject();
        ncParams.put("type", "object");
        JSONObject ncProps = new JSONObject();
        ncProps.put("satellite_type", prop("string", "卫星类型，如 FY4B, Landsat, Sentinel-1, MODIS"));
        ncProps.put("begin_date", prop("string", "开始日期 yyyy-MM-dd"));
        ncProps.put("end_date", prop("string", "结束日期 yyyy-MM-dd"));
        ncProps.put("region_name", prop("string", "区域名称"));
        ncProps.put("img_type", prop("string", "影像类型"));
        ncProps.put("process_type", prop("string", "处理级别"));
        ncProps.put("resolution", prop("string", "空间分辨率"));
        ncProps.put("name", prop("string", "数据文件名关键词"));
        ncProps.put("band_number", prop("integer", "波段数"));
        ncParams.put("properties", ncProps);

        register(ToolMeta.builder("search_nc_data", "DataSearchAgent")
                .description("搜索卫星遥感NC数据")
                .whenToUse("卫星数据检索", "遥感影像查询", "按时间/区域/卫星类型查找数据", "NDVI/LST等分析前的数据准备")
                .whenNotToUse("自然灾害事件查询", "算法模型操作", "地图可视化")
                .inputSchema(ncParams)
                .outputSchema(output("ncList", "NC数据记录列表，每条包含id, ncGeometry, satelliteType等"))
                .build());

        // get_nc_types
        register(ToolMeta.builder("get_nc_types", "DataSearchAgent")
                .description("获取可用的卫星数据类型和筛选选项")
                .whenToUse("用户想知道平台有哪些数据", "不确定卫星类型时先查询")
                .whenNotToUse("已知数据类型直接搜索时")
                .inputSchema(emptySchema())
                .outputSchema(output("types", "卫星类型、影像类型、处理级别等分类列表"))
                .build());

        // search_disasters
        JSONObject disasterParams = new JSONObject();
        disasterParams.put("type", "object");
        JSONObject disasterProps = new JSONObject();
        disasterProps.put("country", prop("string", "国家名称"));
        disasterProps.put("place", prop("string", "地点名称"));
        disasterProps.put("type", prop("string", "灾害类型，如 地震、洪水、台风、火灾"));
        disasterProps.put("begin_date", prop("string", "开始日期 yyyy-MM-dd"));
        disasterProps.put("end_date", prop("string", "结束日期 yyyy-MM-dd"));
        disasterParams.put("properties", disasterProps);

        register(ToolMeta.builder("search_disasters", "DataSearchAgent")
                .description("搜索自然灾害事件记录")
                .whenToUse("灾害事件查询", "查找地震/洪水/台风/火灾等事件", "灾害案例研究")
                .whenNotToUse("卫星遥感数据搜索", "算法分析")
                .inputSchema(disasterParams)
                .outputSchema(output("naturalDisasterList", "灾害记录列表，包含时间、地点、经纬度等"))
                .build());

        // get_disaster_types
        register(ToolMeta.builder("get_disaster_types", "DataSearchAgent")
                .description("获取可用的灾害类型列表")
                .whenToUse("用户想知道有哪些灾害类型", "不确定灾害分类时")
                .whenNotToUse("已知灾害类型直接搜索时")
                .inputSchema(emptySchema())
                .outputSchema(output("types", "灾害类型分类列表"))
                .build());
    }

    private void registerModelAnalysisTools() {
        // list_algorithms
        register(ToolMeta.builder("list_algorithms", "ModelAnalysisAgent")
                .description("列出平台所有可用的分析算法模型（按类别分组）")
                .whenToUse("查看可用算法", "用户想分析数据但不知道用什么算法", "推荐算法时")
                .whenNotToUse("数据搜索", "地图可视化")
                .inputSchema(emptySchema())
                .outputSchema(output("algorithmsByCategory", "按类别分组的算法列表，包含id/name/description/parametersSchema"))
                .build());

        // list_algorithms_by_category
        JSONObject catParams = new JSONObject();
        catParams.put("type", "object");
        JSONObject catProps = new JSONObject();
        catProps.put("category", prop("string", "算法类别: 植被指数、热分析、水体/洪水、灾害监测、土地覆盖、时序分析"));
        catParams.put("properties", catProps);
        catParams.put("required", List.of("category"));

        register(ToolMeta.builder("list_algorithms_by_category", "ModelAnalysisAgent")
                .description("按类别查询算法列表")
                .whenToUse("用户明确了分析方向需要该类别的算法", "NDVI→植被指数类", "火灾→灾害监测类")
                .whenNotToUse("用户不确定分析方向时（应使用list_algorithms）")
                .inputSchema(catParams)
                .outputSchema(output("algorithmList", "该类别下的算法列表"))
                .build());

        // create_dataset
        JSONObject datasetParams = new JSONObject();
        datasetParams.put("type", "object");
        JSONObject datasetProps = new JSONObject();
        datasetProps.put("nc_ids", prop("string", "NC数据ID列表，逗号分隔，如 1,2,3"));
        datasetParams.put("properties", datasetProps);
        datasetParams.put("required", List.of("nc_ids"));

        register(ToolMeta.builder("create_dataset", "ModelAnalysisAgent")
                .description("从NC数据ID列表创建数据集")
                .whenToUse("搜索到数据后要进行分析", "提交算法任务前需要创建数据集")
                .whenNotToUse("只是搜索数据不需要分析", "还没有搜索到数据时")
                .inputSchema(datasetParams)
                .outputSchema(output("success", "数据集创建结果"))
                .build());

        // submit_job
        JSONObject jobParams = new JSONObject();
        jobParams.put("type", "object");
        JSONObject jobProps = new JSONObject();
        jobProps.put("algorithm_id", prop("integer", "算法ID"));
        jobProps.put("data_set_id", prop("integer", "数据集ID"));
        jobProps.put("parameters", prop("string", "算法参数JSON字符串"));
        jobParams.put("properties", jobProps);
        jobParams.put("required", List.of("algorithm_id", "data_set_id"));

        register(ToolMeta.builder("submit_job", "ModelAnalysisAgent")
                .description("提交分析计算任务（可附带参数）")
                .whenToUse("用户确认要用某个算法分析数据", "已有algorithm_id和data_set_id")
                .whenNotToUse("还没有创建数据集", "还没有选择算法")
                .inputSchema(jobParams)
                .outputSchema(output("success", "任务提交结果，包含job_id"))
                .build());

        // list_jobs
        register(ToolMeta.builder("list_jobs", "ModelAnalysisAgent")
                .description("查看已提交的分析任务列表")
                .whenToUse("查询任务状态", "查看历史任务")
                .whenNotToUse("提交新任务")
                .inputSchema(emptySchema())
                .outputSchema(output("jobList", "任务列表，包含状态、算法、时间等"))
                .build());
    }

    private void registerVisualizationTools() {
        // fly_to
        JSONObject flyParams = new JSONObject();
        flyParams.put("type", "object");
        JSONObject flyProps = new JSONObject();
        flyProps.put("longitude", prop("number", "经度"));
        flyProps.put("latitude", prop("number", "纬度"));
        flyParams.put("properties", flyProps);
        flyParams.put("required", List.of("longitude", "latitude"));

        register(ToolMeta.builder("fly_to", "VisualizationAgent")
                .description("地图飞行到指定经纬度位置")
                .whenToUse("定位到某个地点", "查看搜索结果的地理位置", "灾害事件定位")
                .whenNotToUse("加载影像图层", "数据搜索")
                .inputSchema(flyParams)
                .outputSchema(output("action", "FLY_TO可视化指令"))
                .build());

        // show_on_map
        JSONObject mapParams = new JSONObject();
        mapParams.put("type", "object");
        JSONObject mapProps = new JSONObject();
        mapProps.put("imgAddr", prop("string", "影像地址URL"));
        mapProps.put("leftUpPos", prop("object", "左上角坐标 {lon, lat}"));
        mapProps.put("rightBottomPos", prop("object", "右下角坐标 {lon, lat}"));
        mapProps.put("id", prop("string", "影像ID"));
        mapParams.put("properties", mapProps);

        register(ToolMeta.builder("show_on_map", "VisualizationAgent")
                .description("在地图上加载影像图层")
                .whenToUse("显示遥感影像", "可视化分析结果")
                .whenNotToUse("只需要定位不需要加载影像", "搜索数据")
                .inputSchema(mapParams)
                .outputSchema(output("action", "LOAD_IMAGE可视化指令"))
                .build());

        // clear_map
        register(ToolMeta.builder("clear_map", "VisualizationAgent")
                .description("清除地图上所有图层")
                .whenToUse("用户要求清除地图", "开始新的可视化之前")
                .whenNotToUse("用户正在查看图层")
                .inputSchema(emptySchema())
                .outputSchema(output("action", "CLEAR_MAP可视化指令"))
                .build());

        // highlight
        JSONObject hlParams = new JSONObject();
        hlParams.put("type", "object");
        JSONObject hlProps = new JSONObject();
        hlProps.put("id", prop("string", "要高亮的要素ID"));
        hlParams.put("properties", hlProps);

        register(ToolMeta.builder("highlight", "VisualizationAgent")
                .description("高亮指定要素")
                .whenToUse("突出显示某条数据", "用户选择了某条记录")
                .whenNotToUse("加载影像", "搜索数据")
                .inputSchema(hlParams)
                .outputSchema(output("action", "HIGHLIGHT可视化指令"))
                .build());

        // show_table
        register(ToolMeta.builder("show_table", "VisualizationAgent")
                .description("在表格中展示数据")
                .whenToUse("展示搜索结果列表", "显示数据详情")
                .whenNotToUse("地图操作")
                .inputSchema(emptySchema())
                .outputSchema(output("action", "SHOW_TABLE可视化指令"))
                .build());
    }

    private void registerStatisticsTools() {
        // zonal_stats
        JSONObject zonalParams = new JSONObject();
        zonalParams.put("type", "object");
        JSONObject zonalProps = new JSONObject();
        zonalProps.put("result_id", prop("string", "分析结果ID或NC数据ID"));
        zonalProps.put("stat_type", prop("string", "统计类型: mean, max, min, sum, std, histogram"));
        zonalProps.put("aoi", prop("string", "感兴趣区域名称或GeoJSON"));
        zonalParams.put("properties", zonalProps);
        zonalParams.put("required", List.of("result_id", "stat_type"));

        register(ToolMeta.builder("zonal_stats", "StatisticsAgent")
                .description("对分析结果进行区域统计（均值、最大值、面积等）")
                .whenToUse("需要统计某区域的面积/均值", "洪水淹没面积统计", "NDVI均值计算")
                .whenNotToUse("还没有分析结果", "数据搜索阶段")
                .inputSchema(zonalParams)
                .outputSchema(output("statistics", "统计结果: mean, max, min, area等"))
                .build());

        // area_statistics
        JSONObject areaParams = new JSONObject();
        areaParams.put("type", "object");
        JSONObject areaProps = new JSONObject();
        areaProps.put("result_id", prop("string", "分析结果ID"));
        areaProps.put("class_field", prop("string", "分类字段名"));
        areaParams.put("properties", areaProps);
        areaParams.put("required", List.of("result_id"));

        register(ToolMeta.builder("area_statistics", "StatisticsAgent")
                .description("按类别统计面积占比")
                .whenToUse("土地分类面积统计", "烧毁面积估算后的面积统计")
                .whenNotToUse("非分类结果的统计")
                .inputSchema(areaParams)
                .outputSchema(output("areaStats", "各类别面积及占比"))
                .build());

        // time_series_stats
        JSONObject tsParams = new JSONObject();
        tsParams.put("type", "object");
        JSONObject tsProps = new JSONObject();
        tsProps.put("nc_ids", prop("string", "多时相NC数据ID列表，逗号分隔"));
        tsProps.put("band", prop("integer", "统计波段号"));
        tsProps.put("aoi", prop("string", "感兴趣区域"));
        tsParams.put("properties", tsProps);
        tsParams.put("required", List.of("nc_ids"));

        register(ToolMeta.builder("time_series_stats", "StatisticsAgent")
                .description("多时相数据时序统计分析")
                .whenToUse("时序变化趋势分析", "多期数据对比")
                .whenNotToUse("单期数据分析")
                .inputSchema(tsParams)
                .outputSchema(output("timeSeries", "时序统计结果: 各时间点均值/趋势"))
                .build());
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private static JSONObject prop(String type, String description) {
        JSONObject p = new JSONObject();
        p.put("type", type);
        p.put("description", description);
        return p;
    }

    private static JSONObject emptySchema() {
        JSONObject s = new JSONObject();
        s.put("type", "object");
        s.put("properties", new JSONObject());
        return s;
    }

    private static JSONObject output(String key, String description) {
        JSONObject o = new JSONObject();
        o.put("key", key);
        o.put("description", description);
        return o;
    }
}
