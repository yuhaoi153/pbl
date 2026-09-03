package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassTaskDTO {
    private String classNumber;
    private String value;//扫码枪扫码的结果
    private String subject;
    private String content;//单选框的作业类型
    private List<String> images;//作业补充说明
    private String supplementary;//作业补充说明
    private Integer completed;//初始化学生完成作业状态，如果扫码完成的同学，初始化都是未完成
    private LocalDate checkdate;
    private String school;


}
