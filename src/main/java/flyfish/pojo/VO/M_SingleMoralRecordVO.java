package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_SingleMoralRecordVO {
    private Integer id;
    private String grade;
    private Integer className;
    private String supplement;
    private String label;
}
