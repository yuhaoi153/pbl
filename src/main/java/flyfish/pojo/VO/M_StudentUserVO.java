package flyfish.pojo.VO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_StudentUserVO {
    private Integer id;
    private String studentName;
    private String phone;
    private String school;
    private String grade;
    private String className;
    private String studentNumber;
    private String password;
    private Integer year;
    private List<String> phoneList;

}
