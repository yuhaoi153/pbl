package flyfish.pojo.VO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_PersonalCurrentStudentVO {
    private Integer id;
    private String studentName;
    private boolean selected;
}
