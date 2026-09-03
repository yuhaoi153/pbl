package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ExamName {
    private String testName;
    private String createName;
    private String semester;
    private LocalDateTime createTime;
    private String school;
    private String adminName;

}
