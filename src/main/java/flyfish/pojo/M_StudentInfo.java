package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_StudentInfo {
    private Integer id;
    private Integer userId;
    private String studentName;
    private String grade;
    private Integer className;
    private String studentNumber;
    private String school;
    private Integer year;
}
