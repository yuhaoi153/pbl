package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ClubResultByClassVO {
    private Integer orderNum;
    private Integer studentId;
    private String studentName;
    private String clubName;
    private String position;
    private String teacher;
    private String studentClass;
    private Integer clubId;
}
