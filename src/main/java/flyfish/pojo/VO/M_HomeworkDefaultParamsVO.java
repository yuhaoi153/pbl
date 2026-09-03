package flyfish.pojo.VO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_HomeworkDefaultParamsVO {
    private String minusScoreByHomework;//不达标扣分，默认是
    private String failRevisionAddScore;//不达标订正加分，默认是
    private String completedRevisionAddScore;//合格订正加分，默认否
    private Integer pageStudentSize;
    private String showScanner; //是否显示扫码区域，默认是
    private String showCompletedRevision; //是否显示合格待订正，默认否
    private String hiRemind;//展示摄像头区域指引，默认是
    private String homeworkResultRemind;//展示作业登记结果指引，默认是
    private Integer addScoreNumber;//加分分值
    private Integer minusScoreNumber;//扣分分值
    private Integer revisionAddScore;//订正加分分值
    private String school;
    private String userName;

}
