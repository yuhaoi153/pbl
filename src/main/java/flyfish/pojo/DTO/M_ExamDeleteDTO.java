package flyfish.pojo.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class M_ExamDeleteDTO {
    private String examName;
    private String school;
    private String grade;
    private Integer className;
    private String subject;
    private String teacherName;
    private String testName;
    private String semester;
    private String role;
}
