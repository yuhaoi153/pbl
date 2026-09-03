package flyfish.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_SingleMoralRecord {
    private Integer id;
    private String grade;
    private Integer className;
    private String studentName;
    private String supplement;
    private String label;
    private LocalDate checkDate;
    private String school;
    private Integer year;
    private Integer studentId;
}
