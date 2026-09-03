package flyfish.pojo.PBLpojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PBL_VoteDTO {
    private Integer id;
    private String username;
    private String school;
    private String lessonName;//课题名
    private LocalDate checkDate;
    private String questionType;//包括文本填空还是选择等
    private String questionName; //第几题
    private String answerContent;
    private String pageName;//页面几
    private String referenceAnswer;//参考答案
    private String answerType;//回答的类型，包括文本还是语音，还是视频，还是图片等
    private Integer score;
    private String supplementary;
    private Integer questionId;
    private String comment;
    private List<Integer> questionIdList;
}
