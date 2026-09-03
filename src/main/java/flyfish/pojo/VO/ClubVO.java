package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubVO {
    private Integer id;
    private String clubName;
    private String teacher;
    private String description;
    private Integer maxStudents;
    private boolean isActive;

    private String category;
    private String grade;
    private LocalDateTime deadline;
    private String admissionSemester;
    private Integer currentStudents;
    private String position;
    private String finished;

}
