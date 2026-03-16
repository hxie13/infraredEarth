package cn.ac.sitp.infrared.ai.workflow;

import cn.ac.sitp.infrared.ai.model.ExecutionPlan;
import cn.ac.sitp.infrared.ai.model.PlanStep;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Matches user queries against pre-defined workflow templates.
 * When a template matches, returns a ready-to-execute plan without LLM call.
 */
@Component
public class WorkflowMatcher {

    private static final Logger log = LoggerFactory.getLogger(WorkflowMatcher.class);

    private final List<WorkflowTemplate> templates = new ArrayList<>();

    @PostConstruct
    public void init() {
        registerBuiltinTemplates();
        log.info("WorkflowMatcher initialized: {} templates", templates.size());
    }

    /**
     * Try to match user query against templates.
     * Returns matched plan or null if no template matches.
     * Requires minimum 2 keyword hits for a match.
     */
    public ExecutionPlan match(String userQuery) {
        if (userQuery == null || userQuery.isBlank()) return null;

        String query = userQuery.toLowerCase();
        WorkflowTemplate bestMatch = null;
        int bestScore = 0;

        for (WorkflowTemplate template : templates) {
            int score = 0;
            for (String keyword : template.getTriggerKeywords()) {
                if (query.contains(keyword.toLowerCase())) {
                    score++;
                }
            }
            if (score >= 2 && score > bestScore) {
                bestScore = score;
                bestMatch = template;
            }
        }

        if (bestMatch != null) {
            log.info("Workflow template matched: {} (score={})", bestMatch.getId(), bestScore);
            return templateToPlan(bestMatch);
        }

        return null;
    }

    /**
     * Get all registered templates for display/debugging.
     */
    public List<WorkflowTemplate> getAllTemplates() {
        return Collections.unmodifiableList(templates);
    }

    /**
     * Build a prompt fragment listing available workflows for the planning agent.
     */
    public String buildWorkflowCatalogPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("## 常见任务模板\n\n");
        for (WorkflowTemplate t : templates) {
            sb.append("**").append(t.getName()).append("** (").append(t.getId()).append(")\n");
            sb.append("  ").append(t.getDescription()).append("\n");
            sb.append("  关键词: ").append(String.join(", ", t.getTriggerKeywords())).append("\n");
            sb.append("  步骤: ");
            for (int i = 0; i < t.getSteps().size(); i++) {
                if (i > 0) sb.append(" → ");
                sb.append(t.getSteps().get(i).getTool());
            }
            sb.append("\n\n");
        }
        return sb.toString();
    }

    private ExecutionPlan templateToPlan(WorkflowTemplate template) {
        ExecutionPlan plan = new ExecutionPlan();
        for (WorkflowTemplate.WorkflowStep ws : template.getSteps()) {
            JSONObject args = new JSONObject();
            for (Map.Entry<String, String> entry : ws.getArgMapping().entrySet()) {
                args.put(entry.getKey(), entry.getValue());
            }
            plan.addStep(new PlanStep(ws.getAgent(), ws.getTool(), args));
        }
        return plan;
    }

    private void registerBuiltinTemplates() {
        // 1. NDVI植被分析
        templates.add(new WorkflowTemplate(
                "ndvi_analysis", "NDVI植被分析",
                "搜索数据 → 创建数据集 → NDVI算法分析 → 可视化",
                List.of("ndvi", "植被", "植被指数", "vegetation", "绿度"),
                List.of(
                        new WorkflowTemplate.WorkflowStep("DataSearchAgent", "search_nc_data"),
                        new WorkflowTemplate.WorkflowStep("ModelAnalysisAgent", "create_dataset",
                                Map.of("nc_ids", "$step_1")),
                        new WorkflowTemplate.WorkflowStep("ModelAnalysisAgent", "submit_job",
                                Map.of("data_set_id", "$step_2")),
                        new WorkflowTemplate.WorkflowStep("VisualizationAgent", "show_on_map")
                )
        ));

        // 2. 洪水监测
        templates.add(new WorkflowTemplate(
                "flood_monitoring", "洪水监测分析",
                "搜索数据 → 创建数据集 → 洪水淹没范围提取 → 区域统计 → 可视化",
                List.of("洪水", "淹没", "水体", "flood", "积水", "涝"),
                List.of(
                        new WorkflowTemplate.WorkflowStep("DataSearchAgent", "search_nc_data"),
                        new WorkflowTemplate.WorkflowStep("ModelAnalysisAgent", "create_dataset",
                                Map.of("nc_ids", "$step_1")),
                        new WorkflowTemplate.WorkflowStep("ModelAnalysisAgent", "submit_job",
                                Map.of("data_set_id", "$step_2")),
                        new WorkflowTemplate.WorkflowStep("StatisticsAgent", "zonal_stats",
                                Map.of("result_id", "$step_3")),
                        new WorkflowTemplate.WorkflowStep("VisualizationAgent", "show_on_map")
                )
        ));

        // 3. 火灾监测
        templates.add(new WorkflowTemplate(
                "fire_monitoring", "火灾监测分析",
                "搜索数据 → 火点检测/烧毁面积 → 面积统计 → 可视化",
                List.of("火灾", "火点", "烧毁", "fire", "着火", "森林火灾"),
                List.of(
                        new WorkflowTemplate.WorkflowStep("DataSearchAgent", "search_nc_data"),
                        new WorkflowTemplate.WorkflowStep("ModelAnalysisAgent", "create_dataset",
                                Map.of("nc_ids", "$step_1")),
                        new WorkflowTemplate.WorkflowStep("ModelAnalysisAgent", "submit_job",
                                Map.of("data_set_id", "$step_2")),
                        new WorkflowTemplate.WorkflowStep("StatisticsAgent", "area_statistics",
                                Map.of("result_id", "$step_3")),
                        new WorkflowTemplate.WorkflowStep("VisualizationAgent", "show_on_map")
                )
        ));

        // 4. 地表温度分析
        templates.add(new WorkflowTemplate(
                "lst_analysis", "地表温度分析",
                "搜索热红外数据 → LST反演 → 统计 → 可视化",
                List.of("温度", "热", "lst", "热岛", "热红外", "地表温度"),
                List.of(
                        new WorkflowTemplate.WorkflowStep("DataSearchAgent", "search_nc_data"),
                        new WorkflowTemplate.WorkflowStep("ModelAnalysisAgent", "create_dataset",
                                Map.of("nc_ids", "$step_1")),
                        new WorkflowTemplate.WorkflowStep("ModelAnalysisAgent", "submit_job",
                                Map.of("data_set_id", "$step_2")),
                        new WorkflowTemplate.WorkflowStep("StatisticsAgent", "zonal_stats",
                                Map.of("result_id", "$step_3", "stat_type", "mean")),
                        new WorkflowTemplate.WorkflowStep("VisualizationAgent", "show_on_map")
                )
        ));

        // 5. 数据搜索+地图可视化
        templates.add(new WorkflowTemplate(
                "data_search_visualize", "数据搜索并可视化",
                "搜索遥感数据 → 表格展示 → 地图定位",
                List.of("搜索", "查找", "检索", "数据", "卫星", "可视化", "地图"),
                List.of(
                        new WorkflowTemplate.WorkflowStep("DataSearchAgent", "search_nc_data"),
                        new WorkflowTemplate.WorkflowStep("VisualizationAgent", "show_table"),
                        new WorkflowTemplate.WorkflowStep("VisualizationAgent", "fly_to")
                )
        ));

        // 6. 灾害查询+可视化
        templates.add(new WorkflowTemplate(
                "disaster_search_visualize", "灾害查询并可视化",
                "搜索灾害事件 → 表格展示 → 地图定位",
                List.of("灾害", "地震", "台风", "灾害事件", "灾情"),
                List.of(
                        new WorkflowTemplate.WorkflowStep("DataSearchAgent", "search_disasters"),
                        new WorkflowTemplate.WorkflowStep("VisualizationAgent", "show_table"),
                        new WorkflowTemplate.WorkflowStep("VisualizationAgent", "fly_to")
                )
        ));

        // 7. 水体检测
        templates.add(new WorkflowTemplate(
                "water_detection", "水体检测",
                "搜索数据 → NDWI/MNDWI分析 → 可视化",
                List.of("水体", "ndwi", "mndwi", "湖泊", "河流", "water"),
                List.of(
                        new WorkflowTemplate.WorkflowStep("DataSearchAgent", "search_nc_data"),
                        new WorkflowTemplate.WorkflowStep("ModelAnalysisAgent", "create_dataset",
                                Map.of("nc_ids", "$step_1")),
                        new WorkflowTemplate.WorkflowStep("ModelAnalysisAgent", "submit_job",
                                Map.of("data_set_id", "$step_2")),
                        new WorkflowTemplate.WorkflowStep("VisualizationAgent", "show_on_map")
                )
        ));

        // 8. 土地分类
        templates.add(new WorkflowTemplate(
                "land_classification", "土地覆盖分类",
                "搜索数据 → 分类算法 → 面积统计 → 可视化",
                List.of("分类", "土地", "覆盖", "classification", "k-means", "聚类"),
                List.of(
                        new WorkflowTemplate.WorkflowStep("DataSearchAgent", "search_nc_data"),
                        new WorkflowTemplate.WorkflowStep("ModelAnalysisAgent", "create_dataset",
                                Map.of("nc_ids", "$step_1")),
                        new WorkflowTemplate.WorkflowStep("ModelAnalysisAgent", "submit_job",
                                Map.of("data_set_id", "$step_2")),
                        new WorkflowTemplate.WorkflowStep("StatisticsAgent", "area_statistics",
                                Map.of("result_id", "$step_3")),
                        new WorkflowTemplate.WorkflowStep("VisualizationAgent", "show_on_map")
                )
        ));

        // 9. 时序NDVI趋势
        templates.add(new WorkflowTemplate(
                "ndvi_trend", "时序NDVI趋势分析",
                "搜索多期数据 → 时序分析 → 趋势统计",
                List.of("趋势", "时序", "变化", "多期", "mann-kendall", "物候"),
                List.of(
                        new WorkflowTemplate.WorkflowStep("DataSearchAgent", "search_nc_data"),
                        new WorkflowTemplate.WorkflowStep("ModelAnalysisAgent", "create_dataset",
                                Map.of("nc_ids", "$step_1")),
                        new WorkflowTemplate.WorkflowStep("ModelAnalysisAgent", "submit_job",
                                Map.of("data_set_id", "$step_2")),
                        new WorkflowTemplate.WorkflowStep("StatisticsAgent", "time_series_stats",
                                Map.of("nc_ids", "$step_1"))
                )
        ));

        // 10. 变化检测
        templates.add(new WorkflowTemplate(
                "change_detection", "变化检测分析",
                "搜索多期数据 → 变化检测算法 → 面积统计 → 可视化",
                List.of("变化", "检测", "对比", "前后", "change"),
                List.of(
                        new WorkflowTemplate.WorkflowStep("DataSearchAgent", "search_nc_data"),
                        new WorkflowTemplate.WorkflowStep("ModelAnalysisAgent", "create_dataset",
                                Map.of("nc_ids", "$step_1")),
                        new WorkflowTemplate.WorkflowStep("ModelAnalysisAgent", "submit_job",
                                Map.of("data_set_id", "$step_2")),
                        new WorkflowTemplate.WorkflowStep("StatisticsAgent", "area_statistics",
                                Map.of("result_id", "$step_3")),
                        new WorkflowTemplate.WorkflowStep("VisualizationAgent", "show_on_map")
                )
        ));
    }
}
