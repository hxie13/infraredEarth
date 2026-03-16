package cn.ac.sitp.infrared.ai.agent;

import cn.ac.sitp.infrared.ai.llm.LlmClient;
import cn.ac.sitp.infrared.ai.llm.LlmResponse;
import cn.ac.sitp.infrared.ai.model.ChatMessage;
import cn.ac.sitp.infrared.ai.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Fallback agent for general Q&A, platform guidance, and greetings.
 */
@Component
public class ConversationalAgent {

    private static final String SYSTEM_PROMPT = """
            你是红外地球遥感数据平台的AI助手。用中文回答用户问题。

            ## 平台功能介绍
            红外地球平台是一个多尺度数字地球遥感数据管理与分析平台，主要功能包括：

            1. **数据检索**: 搜索全球卫星遥感NC数据（支持按卫星类型、时间范围、区域、分辨率等筛选），以及自然灾害事件检索
            2. **模型分析**: 6大类18种GEE风格算法（植被指数、热分析、水体/洪水、灾害监测、土地覆盖、时序分析）
            3. **统计分析**: 区域统计（面积、均值）、时序趋势分析等
            4. **案例展示**: 浏览灾害案例，查看台风轨迹动画、灾害影响范围等可视化展示

            ## 使用提示
            - 你可以直接用自然语言告诉我你想做什么，例如："帮我找最近一周四川的Sentinel-2影像"
            - 搜索到数据后，可以请求在地图上显示
            - 可以选择算法对搜索到的数据进行分析
            - 模型分析和数据集创建功能需要先登录

            保持回答简洁友好，如果用户的问题超出平台功能范围，礼貌地说明并引导回平台功能。
            """;

    public ChatResponse execute(LlmClient llmClient, List<ChatMessage> history) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(SYSTEM_PROMPT));

        int start = Math.max(0, history.size() - 10);
        for (int i = start; i < history.size(); i++) {
            messages.add(history.get(i));
        }

        LlmResponse response = llmClient.chat(messages, null);
        return ChatResponse.withReply(response.getContent());
    }
}
