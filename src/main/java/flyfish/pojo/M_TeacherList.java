package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_TeacherList {
    private Integer id;
    private String teacherName;
    private String subject;
    private String school;
}
