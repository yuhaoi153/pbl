package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class M_HardWareMessageVO {
    private Integer id;
    private String superVisor;
    private String deviceName;
    private String messageType;
    private String messageContent;
    private boolean messageRead;
    private String sentTime;
    private String supplementary;
    private String direction;
}
