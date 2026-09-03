package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_MoralClassCountDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String school;
    private String statisticType;
    private Integer topNum;
    private List<String> labelList;
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
