package flyfish.pojo.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_QueryStudentAllExamDTO {
    private Integer id;
    private String school;
    private String grade;
    private Integer className;
    private String subject;
    private String studentName;
}
