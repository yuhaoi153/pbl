package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_FeedbackVO {
    private Integer id;
    private String grade;
    private String teacherName;
    private String className;
    private String subject;

}
