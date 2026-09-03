package flyfish.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeek API 配置。
 */
@Component
@Data
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {

    private String apiKey;
    private String url;
    private String model;
    private String thinkingType;
    private String reasoningEffort;
}
