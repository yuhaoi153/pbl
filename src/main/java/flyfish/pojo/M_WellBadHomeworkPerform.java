package flyfish.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_WellBadHomeworkPerform {
    private Integer id;
    private String studentName;
    private Integer homeworkRecordId;
    private Integer scorePerformId;
    private String situation;
    private String showUrl;
    private String className;
    private String subject;
    private String school;
    private Integer year;
    private String supplementary; //如果是作业的话，则对应作业类型content
    private String showItem; //是否展示
    private LocalDate checkDate;
    private LocalDateTime createTime;

}
