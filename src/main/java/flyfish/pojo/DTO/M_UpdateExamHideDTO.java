package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_UpdateExamHideDTO {
    private Integer id;
    private String school;
    private String grade;
    private Integer className;
    private String subject;
    private String studentName;
    private String teacherName;
    private String examName;
    private String hide;


    private String semester;
    private String testName;
}
