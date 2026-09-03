package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_AddNewTeacher {
    private Integer id;
    private String teacherName;
    private String className;
    private String subject;
    private String school;
    private String label;
    private String grade;
    private Integer classNum;
}
