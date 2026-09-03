package flyfish.pojo.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_UpdateExamStudentScoreDTO {
    private Integer id;
    private String school;
    private String grade;
    private Integer className;
    private String subject;
    private String examName;
    private String studentName;
    private String status;
    private BigDecimal score;
    private String teahcerName;
}
