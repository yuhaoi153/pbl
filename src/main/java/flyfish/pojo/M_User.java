package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_User {
    private Integer id;
    private String name;
    private String password;
    private String role;
    private String phone;
    private Integer className;
    private String admissionSemester;
    private String grade;
    private String classNumber;
    private String school;
    private List<String> titleList;
}
