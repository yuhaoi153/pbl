package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ClubStudentInfo {
    private Integer id;
    private String studentName;
    private String studentGrade;
    private String studentClass;
    private Integer firstChoiceId;
    private Integer firstChooseStatus;
    private Integer secondChooseStatus;
    private Integer thirdChooseStatus;
    private Integer secondChoiceId;
    private Integer thirdChoiceId;
    private Integer userId;

}
