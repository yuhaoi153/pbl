package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ClassTeacherRelation {

    private Integer id;
    private String school;
    private String className;
    private String teacherName;
    private int classId;
    private int teacherId;
    private String headTeacher;


}
