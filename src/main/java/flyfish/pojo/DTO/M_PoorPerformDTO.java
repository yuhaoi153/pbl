package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_PoorPerformDTO {
    private String school;
    private String className;
    private String teacherName;
    private String situation;
    private String remark;
    private String grade;
    private LocalDate checkDate;
    private String classPeriod;
    private String subject;


}
