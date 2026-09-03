package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupFeedbackVO {
    private String classNumber;
    private String groupNumber;
    private String subject;
    private String name;
    private Integer addscore;
    private Integer sumscore;
    private Integer punishscore;

}
