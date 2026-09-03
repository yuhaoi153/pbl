package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class M_HardWareOledUploadResultVO {
    private String deviceId;
    private long version;
    private String message;
    private String requestId;
    private Instant createdAt;
    private boolean duplicate;
}
