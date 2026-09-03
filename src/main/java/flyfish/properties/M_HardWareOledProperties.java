package flyfish.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "m-hardware.oled")
public class M_HardWareOledProperties {
    @Min(1)
    @Max(4096)
    private int maxMessageCodePoints = 200;

    @Min(1)
    @Max(16384)
    private int maxMessageUtf8Bytes = 1024;
}
