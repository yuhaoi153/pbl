package flyfish.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ExamNotCountStudent {
    private Integer id;
    private String studentName;
    private String grade;
    private Integer className;
    private Integer year;
    private String school;
    private String supplementary;
}
