package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Perform {
    private LocalDate checkdate;
    private String name;
    private String subject;
    private String reason;
    private String situation;
    private Integer id;
    private Integer score;
    private String classNumber;
    private String school;
    private Integer year;
    private String punishMeasures;//惩罚举措
}
