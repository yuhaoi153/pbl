package flyfish.pojo.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrivateMessageVO {
    private Integer senderId;
    private Integer receiverId;
    private String messageType;
    private String messageContent;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 强制ISO格式
    private LocalDateTime sentAt;
    private boolean messageRead;
    private boolean withdrawn;
    private Integer duration;
    private Integer fileSize;
    private String supplementary;
    private String senderName;
    private Integer messageId;

}
