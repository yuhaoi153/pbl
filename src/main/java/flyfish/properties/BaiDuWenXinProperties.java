package flyfish.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data

@ConfigurationProperties("baidu.wenxin")
public class BaiDuWenXinProperties {
    private String appId;
    private String apiKey;
    private String secretKey;
    private String sdkAccessKey;
    private String sdkSecretKey;
}
