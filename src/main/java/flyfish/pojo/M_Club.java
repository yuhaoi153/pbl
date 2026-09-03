package flyfish.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_Club {
    private Integer id;
    private String clubName;
    private  String teacher;
    private List<String> teacherList;
    private String description;
    private Integer maxStudents;
    private String category;
    private String grade;
    //对日期格式进行统一
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;
    private String admissionSemester;
    private String position;
    private String finished;
}
