package cn.ac.sitp.infrared.ai.llm;

import cn.ac.sitp.infrared.ai.config.AiConfig;
import org.springframework.stereotype.Component;

/**
 * Creates the appropriate LlmClient based on configuration.
 * DeepSeek, OpenAI, and Ollama all use the OpenAI-compatible protocol,
 * so they share the same client implementation with different base URLs.
 */
@Component
public class LlmClientFactory {

    private final AiConfig aiConfig;
    private volatile LlmClient cachedClient;

    public LlmClientFactory(AiConfig aiConfig) {
        this.aiConfig = aiConfig;
    }

    public LlmClient getClient() {
        if (cachedClient == null) {
            synchronized (this) {
                if (cachedClient == null) {
                    cachedClient = createClient();
                }
            }
        }
        return cachedClient;
    }

    private LlmClient createClient() {
        // All three providers (DeepSeek, OpenAI, Ollama) use the OpenAI-compatible API format.
        // The only differences are base URL and auth header.
        return new DeepSeekLlmClient(aiConfig.getLlm());
    }
}
