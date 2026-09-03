package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_GradeClass {
    private Integer id;
    private String grade;
    private String className;
    private String school;
    private LocalDateTime createTime;
}
