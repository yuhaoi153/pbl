package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Charts {
    private String className;
    private Integer praiseClasscount;
    private String subject;
    private Integer praiseSubjectcount;
    private String grade;
    private Integer praiseGradecount;
    private String teacherName;
    private Integer praiseTeachercount;
}
