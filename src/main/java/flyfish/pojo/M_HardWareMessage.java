package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class M_HardWareMessage {
    private Integer id;
    private String superVisor;
    private String deviceName;
    private String messageType;
    private String messageContent;
    private Boolean messageRead;
    private LocalDateTime sentTime;
    private String supplementary;
    private String direction;
}
