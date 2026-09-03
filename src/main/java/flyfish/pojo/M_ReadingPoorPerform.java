package flyfish.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ReadingPoorPerform {

    private Integer id;
    private String grade;
    private String className;
    private String teacherName;
    private String situation;
    private LocalDate checkDate;
}
