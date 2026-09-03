package flyfish.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private Integer messageId;
    private String senderId;      // 发送者
    private String receiverId;    // 接收者（单聊时使用）
    private String classNumber; // 班级号
    private String messageContent;     // 消息内容
    private String messageType; // text,image,phone,audio,file ，默认text
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 强制ISO格式
    private LocalDateTime sentAt;
    private String chatType;   // 消息类型 private 或者group
    private Integer duration; //语音消息时长
    private Integer fileSize; //文件大小
    private String supplementary;
    private String callId;//发起通话的唯一识别吗
    private String senderName;
    private LocalDate checkDate;
    private String audioText;



}