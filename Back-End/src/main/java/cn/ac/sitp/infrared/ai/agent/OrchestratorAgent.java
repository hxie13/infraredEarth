package cn.ac.sitp.infrared.ai.agent;

import cn.ac.sitp.infrared.ai.llm.LlmClient;
import cn.ac.sitp.infrared.ai.llm.LlmResponse;
import cn.ac.sitp.infrared.ai.model.ChatMessage;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Classifies user intent and routes to the appropriate agent/planning workflow.
 */
@Component
public class OrchestratorAgent {

    private static final Logger log = LoggerFactory.getLogger(OrchestratorAgent.class);

    private static final String SYSTEM_PROMPT = """
            你是红外地球遥感数据平台的意图分类器。根据用户消息，将其分类为以下类别之一：

            - DATA_SEARCH: 搜索卫星遥感数据(NC数据)或自然灾害事件，包括查询可用数据集、卫星类型等
            - MODEL_ANALYSIS: 选择算法模型、创建数据集、提交分析计算任务、查询任务状态
            - STATISTICS: 统计分析，如区域面积统计、均值统计、时序趋势分析
            - VISUALIZATION: 在地图上显示数据、飞行定位到指定区域、高亮要素、清除地图
            - GENERAL: 平台帮助、问候、功能介绍、通用问题
            - MULTI_STEP: 需要多个步骤的复合请求，例如"搜索数据并进行分析"、"找到灾害数据并在地图上显示"、"计算NDVI并统计面积"

            对于 MULTI_STEP 类别，还需要输出按执行顺序排列的子意图列表。

            严格按以下 JSON 格式返回，不要包含其他文字：
            {"intent": "类别名", "sub_intents": ["子意图1", "子意图2"]}

            如果不是 MULTI_STEP，sub_intents 返回空数组。
            """;

    public IntentResult classify(LlmClient llmClient, List<ChatMessage> conversationHistory) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(SYSTEM_PROMPT));

        int start = Math.max(0, conversationHistory.size() - 4);
        for (int i = start; i < conversationHistory.size(); i++) {
            ChatMessage msg = conversationHistory.get(i);
            if ("user".equals(msg.getRole()) || "assistant".equals(msg.getRole())) {
                messages.add(msg);
            }
        }

        LlmResponse response = llmClient.chat(messages, null);
        return parseIntent(response.getContent());
    }

    private IntentResult parseIntent(String content) {
        if (content == null || content.isBlank()) {
            return new IntentResult(AgentIntent.GENERAL, List.of());
        }

        try {
            String json = content.trim();
            if (json.contains("{")) {
                json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
            }
            JSONObject obj = JSON.parseObject(json);
            String intentStr = obj.getString("intent");
            AgentIntent intent = AgentIntent.valueOf(intentStr);

            List<AgentIntent> subIntents = new ArrayList<>();
            JSONArray subs = obj.getJSONArray("sub_intents");
            if (subs != null) {
                for (int i = 0; i < subs.size(); i++) {
                    try {
                        subIntents.add(AgentIntent.valueOf(subs.getString(i)));
                    } catch (IllegalArgumentException ignored) {}
                }
            }

            return new IntentResult(intent, subIntents);
        } catch (Exception e) {
            log.warn("Failed to parse intent from LLM response: {}", content, e);
            return fallbackClassify(content);
        }
    }

    private IntentResult fallbackClassify(String content) {
        String lower = content.toLowerCase();
        if (lower.contains("data_search") || lower.contains("搜索") || lower.contains("查找") || lower.contains("检索")) {
            return new IntentResult(AgentIntent.DATA_SEARCH, List.of());
        }
        if (lower.contains("model") || lower.contains("分析") || lower.contains("算法") || lower.contains("ndvi")) {
            return new IntentResult(AgentIntent.MODEL_ANALYSIS, List.of());
        }
        if (lower.contains("statistic") || lower.contains("统计") || lower.contains("面积") || lower.contains("均值")) {
            return new IntentResult(AgentIntent.STATISTICS, List.of());
        }
        if (lower.contains("visual") || lower.contains("地图") || lower.contains("显示")) {
            return new IntentResult(AgentIntent.VISUALIZATION, List.of());
        }
        return new IntentResult(AgentIntent.GENERAL, List.of());
    }

    public record IntentResult(AgentIntent intent, List<AgentIntent> subIntents) {}
}
