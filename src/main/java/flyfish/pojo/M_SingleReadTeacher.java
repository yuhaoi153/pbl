package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_SingleReadTeacher {
    private String grade;
    private Integer id;
    private String school;
    private String subject;
    private String teacherName;
    private String timeZone;
    private LocalDate checkDate;
    private LocalDateTime createTime;
    private String className;
}
