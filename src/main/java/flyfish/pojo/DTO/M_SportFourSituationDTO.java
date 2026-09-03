package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_SportFourSituationDTO {


        private String school;
        private List<String> gymPraiseClassNameList;
        private List<String> gymCriticizeClassNameList;
        private List<String> runPraiseClassNameList;
        private List<String> runCriticizeClassNameList;
        private LocalDate checkDate;
        private String grade;//前端传递的年级标签
        private String label;//前端传递的标签


}
