package flyfish.pojo.VO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomeworkMessageVO {
    private Integer id;
    private Integer senderId;
    private Integer receiverId;
    private String messageType;
    private String messageContent;
    private LocalDateTime sentAt;
    private LocalDate checkDate;
    private boolean messageRead;
    private boolean withdrawn;
    private String subject;
    private String supplementary;
}
