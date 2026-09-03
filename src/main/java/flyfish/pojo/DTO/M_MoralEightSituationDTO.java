package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_MoralEightSituationDTO {
    private String school;
    private String grade;//前端传递的年级标签
    private String label;//前端传递的标签
    private String supplement;//前端传递的补充标签
    private LocalDate checkDate;
    private List<String> roadPraiseClassNameList; //路队表扬班级列表
    private List<String> roadCriticizeClassNameList;//路队批评班级列表
    private List<String> disciplinePraiseClassNameList;//纪律表扬班级列表
    private List<String> disciplineCriticizeClassNameList;//纪律批评班级列表
    private List<String> hygienePraiseClassNameList;//卫生表扬班级列表
    private List<String> hygieneCriticizeClassNameList;//卫生批评班级列表
    private  List<String> personalPraiseClassNameList;//个人表扬班级列表
    private  List<String> personalCriticizeClassNameList;//个人批评班级列表

}
