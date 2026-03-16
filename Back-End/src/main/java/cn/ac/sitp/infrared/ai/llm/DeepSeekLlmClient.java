package cn.ac.sitp.infrared.ai.llm;

import cn.ac.sitp.infrared.ai.config.AiConfig;
import cn.ac.sitp.infrared.ai.model.ChatMessage;
import cn.ac.sitp.infrared.ai.model.ToolCall;
import cn.ac.sitp.infrared.ai.model.ToolDefinition;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * LLM client for DeepSeek / OpenAI-compatible APIs.
 * Works with DeepSeek, OpenAI, and any OpenAI-compatible endpoint.
 */
public class DeepSeekLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekLlmClient.class);

    private final AiConfig.Llm config;
    private final CloseableHttpClient httpClient;

    public DeepSeekLlmClient(AiConfig.Llm config) {
        this.config = config;
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setResponseTimeout(Timeout.of(config.getTimeoutSeconds(), TimeUnit.SECONDS))
                        .setConnectionRequestTimeout(Timeout.of(10, TimeUnit.SECONDS))
                        .build())
                .build();
    }

    @Override
    public LlmResponse chat(List<ChatMessage> messages, List<ToolDefinition> tools) {
        try {
            JSONObject requestBody = buildRequestBody(messages, tools);
            String url = config.getBaseUrl().replaceAll("/+$", "") + "/v1/chat/completions";

            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + config.getApiKey());
            httpPost.setEntity(new StringEntity(requestBody.toJSONString(), ContentType.APPLICATION_JSON));

            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                String body = EntityUtils.toString(response.getEntity());

                if (statusCode != 200) {
                    log.error("LLM API error: status={}, body={}", statusCode, body);
                    return LlmResponse.text("AI 服务暂时不可用，请稍后再试。(HTTP " + statusCode + ")");
                }

                return parseResponse(body);
            });
        } catch (Exception e) {
            log.error("LLM API call failed", e);
            return LlmResponse.text("AI 服务连接失败，请检查配置。");
        }
    }

    private JSONObject buildRequestBody(List<ChatMessage> messages, List<ToolDefinition> tools) {
        JSONObject body = new JSONObject();
        body.put("model", config.getModel());
        body.put("temperature", config.getTemperature());
        body.put("max_tokens", config.getMaxTokens());

        JSONArray messagesArray = new JSONArray();
        for (ChatMessage msg : messages) {
            messagesArray.add(msg.toJson());
        }
        body.put("messages", messagesArray);

        if (tools != null && !tools.isEmpty()) {
            JSONArray toolsArray = new JSONArray();
            for (ToolDefinition tool : tools) {
                toolsArray.add(tool.toJson());
            }
            body.put("tools", toolsArray);
            body.put("tool_choice", "auto");
        }

        return body;
    }

    private LlmResponse parseResponse(String body) {
        JSONObject json = JSON.parseObject(body);
        JSONArray choices = json.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            return LlmResponse.text("AI 未返回有效响应。");
        }

        JSONObject choice = choices.getJSONObject(0);
        JSONObject message = choice.getJSONObject("message");
        if (message == null) {
            return LlmResponse.text("AI 未返回有效响应。");
        }

        // Check for tool calls
        JSONArray toolCallsJson = message.getJSONArray("tool_calls");
        if (toolCallsJson != null && !toolCallsJson.isEmpty()) {
            List<ToolCall> toolCalls = new ArrayList<>();
            for (int i = 0; i < toolCallsJson.size(); i++) {
                toolCalls.add(ToolCall.fromJson(toolCallsJson.getJSONObject(i)));
            }
            return LlmResponse.withToolCalls(toolCalls);
        }

        // Plain text response
        String content = message.getString("content");
        return LlmResponse.text(content != null ? content : "");
    }
}
