package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupFeedbackDTO {
    private String classNumber;
    private String subject;
    private String value;
    private String group;
    private Integer score;
    private String school;
}
