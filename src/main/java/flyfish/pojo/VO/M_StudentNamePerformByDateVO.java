package flyfish.pojo.VO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class M_StudentNamePerformByDateVO {
    private String name;
    private LocalDate checkdate;
    private String subject;
    private Integer homeworkPraiseCount;
    private Integer homeworkCriticizeCount;
    private Integer classPraiseCount;
    private Integer classCriticizeCount;
    private Integer convertCount;
    private Integer homeworkCount;
    private Integer classCount;
}
