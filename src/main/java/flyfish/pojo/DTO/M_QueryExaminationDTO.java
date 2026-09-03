package flyfish.pojo.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class M_QueryExaminationDTO {
    private Integer id;
    private String school;
    private String grade;
    private Integer className;
    private String subject;
    private String examName;
    private String teacherName;
    List<String> historyExamNameList;
}
