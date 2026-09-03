package flyfish.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("aliyun.directmail")
public class DirectMailProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private String accountName;
}
