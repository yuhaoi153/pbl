package flyfish.pojo.PBLpojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PBL_QuestionItem {
    private Integer id;
    private Integer questionId;
    private String content;
    private String referenceAnswer;
    private String pageName;
    private String questionType;
    private String questionLevel;
    private Integer score;
    private String lessonName;
    private String subject;
    private String school;
    private String username;
    private LocalDate checkDate;
}
