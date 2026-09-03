package flyfish.pojo.VO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_TeacherUserVO {
    private Integer id;
    private String teacherName;
    private String phone;
    private String school;
    private List<String> titleList;
    private String password;
    private String grade;
    private List<String> classNameList;
    private String subject;
    private List<String> gradeList;


}
