package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class M_HardWareOledNotification {
    private String deviceId;
    private long version;
    private String message;
    private String requestId;
    private Instant createdAt;
}
