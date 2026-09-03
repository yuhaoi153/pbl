package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_FeedBackReportVO {
    private String school;
    private Integer id;
    private String preface;
    private String preClassManage;
    private String postClassManage;
    private String preTeacherManage;
    private String postTeacherManage;
    private  String preClassReading;
    private String postClassReading;
    private String preTeacherReading;
    private String postTeacherReading;
    private String preClassPrepare;
    private String postClassPrepare;
    private String preTeacherPrepare;
    private String postTeacherPrepare;
    private String finalComment;

}
