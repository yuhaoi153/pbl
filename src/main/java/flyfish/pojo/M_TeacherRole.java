package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_TeacherRole {
    private Integer id;
    private String teacherName;
    private String title;
    private String school;
    private Integer userId;
}
