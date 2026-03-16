package cn.ac.sitp.infrared.ai.agent;

import cn.ac.sitp.infrared.ai.llm.LlmClient;
import cn.ac.sitp.infrared.ai.llm.LlmResponse;
import cn.ac.sitp.infrared.ai.model.*;
import cn.ac.sitp.infrared.datasource.dao.Algorithm;
import cn.ac.sitp.infrared.datasource.dao.AxrrAccount;
import cn.ac.sitp.infrared.service.JobService;
import cn.ac.sitp.infrared.service.NCService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Agent for algorithm selection, dataset creation, and job submission.
 * Supports 18 GEE-style algorithms across 6 categories.
 */
@Component
@RequiredArgsConstructor
public class ModelAnalysisAgent {

    private static final Logger log = LoggerFactory.getLogger(ModelAnalysisAgent.class);
    private static final long PROMPT_CACHE_TTL_MS = 10 * 60 * 1000; // 10 minutes

    private final JobService jobService;
    private final NCService ncService;

    // Cache the system prompt since algorithm catalog rarely changes
    private final AtomicReference<CachedPrompt> promptCacheRef = new AtomicReference<>();

    private String buildSystemPrompt() {
        CachedPrompt cached = promptCacheRef.get();
        if (cached != null && !cached.isExpired()) {
            return cached.prompt;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("""
                你是红外地球遥感平台的模型分析助手。帮助用户选择算法、创建数据集和提交分析任务。

                平台提供6大类18种GEE风格遥感分析算法：

                """);

        try {
            List<Algorithm> algorithms = jobService.getAllActiveAlgorithms();
            if (algorithms != null && !algorithms.isEmpty()) {
                Map<String, List<Algorithm>> grouped = algorithms.stream()
                        .collect(Collectors.groupingBy(
                                a -> a.getCategory() != null ? a.getCategory() : "其他",
                                LinkedHashMap::new, Collectors.toList()));

                for (Map.Entry<String, List<Algorithm>> entry : grouped.entrySet()) {
                    sb.append("【").append(entry.getKey()).append("】\n");
                    for (Algorithm alg : entry.getValue()) {
                        sb.append("  - ID=").append(alg.getId())
                                .append(" ").append(alg.getName());
                        if (alg.getDescription() != null) {
                            sb.append(": ").append(alg.getDescription());
                        }
                        sb.append("\n");
                    }
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load algorithm catalog for system prompt", e);
            sb.append("（算法目录加载失败，请使用 list_algorithms 工具查询）\n\n");
            // Don't cache failed prompts
            return sb.toString();
        }

        sb.append("""
                工作流程：
                1. 如果用户想要分析数据，先查询可用算法列表或按类别筛选
                2. 根据用户需求推荐合适的算法，说明算法原理和适用场景
                3. 需要先创建数据集（从之前搜索的NC数据ID列表创建）
                4. 然后提交分析任务（需要 algorithm_id、data_set_id，可选 parameters JSON）

                注意：创建数据集和提交任务需要用户已登录。如果操作失败提示未登录，告知用户需要先登录。
                """);

        String prompt = sb.toString();
        promptCacheRef.set(new CachedPrompt(prompt, System.currentTimeMillis()));
        return prompt;
    }

    // Tool definitions are static — build once and reuse
    private static final List<ToolDefinition> TOOL_DEFINITIONS = buildToolDefinitions();

    private static List<ToolDefinition> buildToolDefinitions() {
        List<ToolDefinition> tools = new ArrayList<>();

        JSONObject emptyParams = new JSONObject();
        emptyParams.put("type", "object");
        emptyParams.put("properties", new JSONObject());
        tools.add(new ToolDefinition("list_algorithms", "列出平台所有可用的分析算法模型（按类别分组）", emptyParams));

        JSONObject catParams = new JSONObject();
        catParams.put("type", "object");
        JSONObject catProps = new JSONObject();
        JSONObject catP = new JSONObject();
        catP.put("type", "string");
        catP.put("description", "算法类别，如: 植被指数、热分析、水体/洪水、灾害监测、土地覆盖、时序分析");
        catProps.put("category", catP);
        catParams.put("properties", catProps);
        catParams.put("required", List.of("category"));
        tools.add(new ToolDefinition("list_algorithms_by_category", "按类别查询算法列表", catParams));

        JSONObject datasetParams = new JSONObject();
        datasetParams.put("type", "object");
        JSONObject datasetProps = new JSONObject();
        JSONObject ncIdsP = new JSONObject();
        ncIdsP.put("type", "string");
        ncIdsP.put("description", "NC数据ID列表，逗号分隔，如 1,2,3");
        datasetProps.put("nc_ids", ncIdsP);
        datasetParams.put("properties", datasetProps);
        datasetParams.put("required", List.of("nc_ids"));
        tools.add(new ToolDefinition("create_dataset", "从NC数据ID列表创建数据集", datasetParams));

        JSONObject jobParams = new JSONObject();
        jobParams.put("type", "object");
        JSONObject jobProps = new JSONObject();
        JSONObject algIdP = new JSONObject();
        algIdP.put("type", "integer");
        algIdP.put("description", "算法ID");
        jobProps.put("algorithm_id", algIdP);
        JSONObject dsIdP = new JSONObject();
        dsIdP.put("type", "integer");
        dsIdP.put("description", "数据集ID");
        jobProps.put("data_set_id", dsIdP);
        JSONObject paramsP = new JSONObject();
        paramsP.put("type", "string");
        paramsP.put("description", "算法参数JSON字符串，根据算法的parameters_schema填写");
        jobProps.put("parameters", paramsP);
        jobParams.put("properties", jobProps);
        jobParams.put("required", List.of("algorithm_id", "data_set_id"));
        tools.add(new ToolDefinition("submit_job", "提交分析计算任务（可附带参数）", jobParams));

        tools.add(new ToolDefinition("list_jobs", "查看已提交的分析任务列表", emptyParams));

        return Collections.unmodifiableList(tools);
    }

    public ChatResponse execute(LlmClient llmClient, List<ChatMessage> history, AxrrAccount user) {
        if (user == null) {
            return ChatResponse.withReply("模型分析功能需要登录后使用。请先在页面右上角登录您的账户。");
        }

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(buildSystemPrompt()));

        int start = Math.max(0, history.size() - 10);
        for (int i = start; i < history.size(); i++) {
            messages.add(history.get(i));
        }

        LlmResponse response = llmClient.chat(messages, TOOL_DEFINITIONS);

        if (!response.hasToolCalls()) {
            return ChatResponse.withReply(response.getContent());
        }

        ChatResponse chatResponse = new ChatResponse();
        messages.add(ChatMessage.assistantWithToolCalls(response.getToolCalls()));

        for (ToolCall toolCall : response.getToolCalls()) {
            String toolName = toolCall.getFunction().getName();
            String argsJson = toolCall.getFunction().getArguments();
            Map<String, Object> result = executeTool(toolName, argsJson, user);

            String resultJson = JSON.toJSONString(result);
            messages.add(ChatMessage.toolResult(toolCall.getId(), toolName, resultJson));
            chatResponse.setData(result);
        }

        LlmResponse summary = llmClient.chat(messages, null);
        chatResponse.setReply(summary.getContent());
        return chatResponse;
    }

    public Map<String, Object> executeTool(String toolName, String argsJson, AxrrAccount user) {
        JSONObject args = argsJson != null && !argsJson.isBlank()
                ? JSON.parseObject(argsJson) : new JSONObject();

        return switch (toolName) {
            case "list_algorithms" -> {
                List<Algorithm> all = jobService.getAllActiveAlgorithms();
                Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
                for (Algorithm alg : all) {
                    String cat = alg.getCategory() != null ? alg.getCategory() : "其他";
                    grouped.computeIfAbsent(cat, k -> new ArrayList<>())
                            .add(algorithmToMap(alg));
                }
                yield Map.of("algorithmsByCategory", grouped, "total", all.size());
            }
            case "list_algorithms_by_category" -> {
                String category = args.getString("category");
                List<Algorithm> algorithms = jobService.getAlgorithmsByCategory(category);
                yield Map.of("algorithmList", algorithms.stream().map(this::algorithmToMap).toList(),
                        "category", category != null ? category : "all",
                        "count", algorithms.size());
            }
            case "create_dataset" -> executeCreateDataset(args, user);
            case "submit_job" -> executeSubmitJob(args, user);
            case "list_jobs" -> jobService.getJobList(1, 20, user);
            default -> Map.of("error", "Unknown tool: " + toolName);
        };
    }

    private Map<String, Object> algorithmToMap(Algorithm alg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", alg.getId());
        m.put("name", alg.getName());
        m.put("description", alg.getDescription());
        m.put("category", alg.getCategory());
        m.put("inputType", alg.getInputType());
        m.put("outputType", alg.getOutputType());
        if (alg.getParametersSchema() != null) {
            m.put("parametersSchema", alg.getParametersSchema());
        }
        return m;
    }

    private Map<String, Object> executeCreateDataset(JSONObject args, AxrrAccount user) {
        String ncIdsStr = args.getString("nc_ids");
        if (ncIdsStr == null || ncIdsStr.isBlank()) {
            return Map.of("error", "请提供要创建数据集的NC数据ID列表");
        }

        List<Long> ncIds = new ArrayList<>();
        for (String id : ncIdsStr.split(",")) {
            try {
                ncIds.add(Long.parseLong(id.trim()));
            } catch (NumberFormatException ignored) {}
        }

        if (ncIds.isEmpty()) {
            return Map.of("error", "无效的NC数据ID列表");
        }

        try {
            Long dataSetId = ncService.addDataset(ncIds, user);
            return Map.of("success", true, "message", "数据集创建成功",
                    "dataSetId", dataSetId, "nc_count", ncIds.size());
        } catch (Exception e) {
            log.error("Failed to create dataset", e);
            return Map.of("error", "创建数据集失败: " + e.getMessage());
        }
    }

    private Map<String, Object> executeSubmitJob(JSONObject args, AxrrAccount user) {
        Long algorithmId = args.getLong("algorithm_id");
        Long dataSetId = args.getLong("data_set_id");

        if (algorithmId == null || dataSetId == null) {
            return Map.of("error", "请提供算法ID和数据集ID");
        }

        try {
            String parameters = args.getString("parameters");
            Long jobId;
            if (parameters != null && !parameters.isBlank()) {
                jobId = jobService.addJobWithParams(dataSetId, algorithmId, user, parameters);
            } else {
                jobId = jobService.addJob(dataSetId, algorithmId, user);
            }
            jobService.simulateJobExecution(jobId);
            return Map.of("success", true, "message", "分析任务已提交",
                    "jobId", jobId, "algorithm_id", algorithmId, "data_set_id", dataSetId);
        } catch (Exception e) {
            log.error("Failed to submit job", e);
            return Map.of("error", "提交任务失败: " + e.getMessage());
        }
    }

    private record CachedPrompt(String prompt, long createdAt) {
        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > PROMPT_CACHE_TTL_MS;
        }
    }
}
