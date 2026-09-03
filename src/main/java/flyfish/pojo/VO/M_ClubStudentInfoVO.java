package flyfish.pojo.VO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ClubStudentInfoVO {
    private Integer id;

    private String studentName;
    private String studentGrade;
    private String studentClass;
    private String className;
    private Integer firstChoiceId;
    private String firstChoiceName;
    private Integer firstChooseStatus;
    private Integer secondChoiceId;
    private String secondChoiceName;
    private Integer secondChooseStatus;
    private Integer thirdChoiceId;
    private String thirdChoiceName;
    private Integer thirdChooseStatus;
    private String assignedClubName;
    private String phone;
    private List<String> phoneList;
    private Integer userId;
    private String admissionSemester;
    private Integer assignedClubId;
}
