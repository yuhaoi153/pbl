package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordDTO {
    private String classNumber;
    private String value;//扫码枪扫码的结果
    private List<String> valueList;//扫码记录列表
    private String subject;
    private String content;//单选框的作业类型
    private String supplementary;//作业补充说明
    private String type;//课后作业还是课堂练习
    private Integer completed;//初始化学生完成作业状态，如果扫码完成的同学，初始化都是未完成
    private LocalDate checkdate;
    private Integer startpage;
    private Integer endpage;
    private Integer level;//作业登记，0合格，-1不达标，1优秀
    private boolean score;
    private String userName;
    private String school;
    private String minusScoreByHomework;//不达标作业扣分，默认是
    private String failRevisionAddScore;//不达标同学订正作业加分，默认是
    private Integer addScoreNumber;//优秀作业加分分值
    private Integer minusScoreNumber;//不达标作业扣分分值
    private String completedRevisionAddScore;//合格作业是否加分，默认否
    private Integer revisionAddScore;//订正加分分值，默认是1分。

    private String studentName;//网页端传递的姓名数据
    private String fromCategory;//网页端传递过来的修改请求，分别是未达标待订正，合格待订正，未提交



}
