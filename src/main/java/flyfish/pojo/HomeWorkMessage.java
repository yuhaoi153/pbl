package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomeWorkMessage {
    private Integer senderId;
    private Integer receiverId;
    private String messageType;
    private String messageContent;
    private LocalDateTime sentAt;
    private String subject;
    private String className;
    private String school;
    private LocalDate checkDate;
    private String userName;
    private String supplementary;
    private Integer fileSize;
    private Integer duration;


}
