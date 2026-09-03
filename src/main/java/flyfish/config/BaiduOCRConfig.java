package flyfish.config;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaiduOCRConfig {
    
    @Value("${baidu.ocr.api-key}")
    private String apiKey;
    
    @Value("${baidu.ocr.secret-key}")
    private String secretKey;
    
    @Value("${baidu.ocr.access-token-url}")
    private String accessTokenUrl;
    
    @Value("${baidu.ocr.ocr-url}")
    private String ocrUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

}