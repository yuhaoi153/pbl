package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {

    private String classNumber;
    private  String subject;
    private boolean scopeOfTeacher;
    private LocalDate checkdate;
    private String feedbackText;
    private String feedbackPhoneText;
    private String school;
}
