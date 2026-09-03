package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_StudentExamScoreVO {
    private Integer id;
    private String studentName;
    private BigDecimal score;//学生成绩
    private String hide;//是否发布
    private String status;//考试状态
    private String imageUrl;//图片链接
    private BigDecimal averageScore;//平均成绩
    private BigDecimal previousScore;//上次成绩
    private BigDecimal previousAverageScore;//上次平均成绩
    private String currentExamName;//本次考试名称
    private String previousExamName;//上次考试名称
    private Integer rankLevel;//排名等级
    private Integer previousRankLevel;//上次排名等级
}
