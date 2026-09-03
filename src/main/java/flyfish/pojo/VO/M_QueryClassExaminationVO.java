package flyfish.pojo.VO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_QueryClassExaminationVO {
    //平均分
    private BigDecimal averageScore;
    private BigDecimal privisousAverageScore;//上次考试平均分
    //最高
    private BigDecimal maxScore;
    private BigDecimal minScore;
    //最低
    //分数段人数
    private Integer distinctionNum;//优秀人数90-100
    private Integer aboveAverageNum;//良好人数80-89
    private Integer averageNum;//达标人数60-79
    private Integer belowAverageNum;//不达标人数<60
    private Integer watchListNum;//重点关注人数<40
    //年级均分
    private BigDecimal grageAverage;//年级均分
    //上次考试名称
    private String examtName;//当前考试名称

    private String priviousExamName;//上次考试名称
    //实考人数
    private Integer actualTestNum;//实考人数
    //缺考人数
    private Integer absentTestNum;//缺考人数
    //免考人数
    private Integer exemptedTestNum;//免考人数
    //要在数据库中设置，年级统一设置
    private Integer rankAPlus;//前5%（包含低位）
    private Integer rankA;//5%-25%（包含低位）
    private Integer rankB;//25%-50%（包含低位）
    private Integer rankC;//50%-75%（包含低位）
    private Integer rankD;//75%-100%（包含低位）

    private String status;



    String grade;
    Integer className;


    //四分位数据
    private BigDecimal q1Score;
    private BigDecimal medianScore;
    private BigDecimal q3Score;
    //标准箱体图的上下须
    private BigDecimal lowerWhisker;
    private BigDecimal upperWhisker;
    //异常分数
    private List<BigDecimal> outlierScores;

}
