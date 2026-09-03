package flyfish.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "m-hardware.mqtt")
public class M_HardWareMqttProperties {
    private String brokerUri = "tcp://broker.emqx.io:1883";
    private String publisherClientId = "flyfish-backend";
    private String topicPrefix = "flyfish/hardware";
    private int qos = 1;
    private boolean retained = true;
}
