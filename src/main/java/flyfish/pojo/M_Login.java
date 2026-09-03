package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_Login {
    private Integer id;
    private String username;
    private String name;
    private String password;
    private String school;
    private String role;
    private String phone;
    private String status;
    private String grade;
    private String studentClassName;
    private String headTeacherClassName;
    private List<String> teacherClassNameList;
    private String subject;

}
