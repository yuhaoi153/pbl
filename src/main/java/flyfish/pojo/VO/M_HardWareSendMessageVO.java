package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class M_HardWareSendMessageVO {
    private Integer messageId;
    private String deviceName;
    private String topic;
    private String superVisor;
    private String messageType;
    private String messageContent;
    private String sentTime;
    private boolean published;
}
