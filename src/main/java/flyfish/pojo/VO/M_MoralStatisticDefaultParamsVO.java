package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_MoralStatisticDefaultParamsVO {
    private String school;
    private List<String> labelList;
    private Integer topNum;
    private Integer roadPraiseAddScore;
    private Integer roadCriticizeSubScore;
    private Integer disciplinePraiseAddScore;
    private Integer disciplineCriticizeSubScore;
    private Integer hygienePraiseAddScore;
    private Integer hygieneCriticizeSubScore;
    private Integer personalPraiseAddScore;
    private Integer personalCriticizeSubScore;
    private Integer gymPraiseAddScore;
    private Integer gymCriticizeSubScore;
    private Integer runPraiseAddScore;
    private Integer runCriticizeSubScore;
    private Integer selfmanagePraiseAddScore;
    private Integer readPraiseAddScore;
    private Integer lessonPraiseAddScore;
}
