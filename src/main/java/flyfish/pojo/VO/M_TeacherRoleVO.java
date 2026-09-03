package flyfish.pojo.VO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_TeacherRoleVO {
    private Integer id;
    private String teacherName;
    private String school;
    private String title;
    private String className;
    private String subject;
    private String grade;

}
