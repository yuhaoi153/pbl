package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class M_HardWareMessagePollVO {
    private Integer deviceId;
    private String deviceName;
    private Integer latestId;
    private List<M_HardWareMessageVO> messages;
}
