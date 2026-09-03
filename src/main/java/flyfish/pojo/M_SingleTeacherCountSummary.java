package flyfish.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_SingleTeacherCountSummary {

    private String subject;
    private String teacherName;
    private LocalDate checkDate;
    private Integer countTeacherName;
}
