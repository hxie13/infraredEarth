package cn.ac.sitp.infrared.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {

    private final Llm llm = new Llm();
    private final Conversation conversation = new Conversation();

    public Llm getLlm() { return llm; }
    public Conversation getConversation() { return conversation; }

    public static class Llm {
        private String provider = "deepseek";
        private String apiKey = "";
        private String baseUrl = "https://api.deepseek.com";
        private String model = "deepseek-chat";
        private double temperature = 0.3;
        private int maxTokens = 2048;
        private int timeoutSeconds = 30;

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    }

    public static class Conversation {
        private int maxHistory = 20;

        public int getMaxHistory() { return maxHistory; }
        public void setMaxHistory(int maxHistory) { this.maxHistory = maxHistory; }
    }
}
