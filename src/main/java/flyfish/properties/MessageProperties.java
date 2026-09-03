package flyfish.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("aliyun.single-message")
public class MessageProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private String signName;
    private String templateCode;
    private String templateCode2;
}
