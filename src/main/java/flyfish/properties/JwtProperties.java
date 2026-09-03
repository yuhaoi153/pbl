package flyfish.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sky.jwt")
public class JwtProperties {
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;
}
