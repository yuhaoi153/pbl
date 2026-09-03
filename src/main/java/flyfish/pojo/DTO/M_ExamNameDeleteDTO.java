package flyfish.pojo.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ExamNameDeleteDTO {
    private Integer id;
    private String role;
    private String teacherName;
}
