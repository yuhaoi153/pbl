package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class M_HardWareOledNotificationVO {
    private String deviceId;
    private long version;
    private String message;
    private Instant createdAt;
}
