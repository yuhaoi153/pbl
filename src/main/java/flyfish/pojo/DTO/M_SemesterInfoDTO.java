package flyfish.pojo.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_SemesterInfoDTO {
    private Integer id;
    private String semester;
    private String infoName;
    private String school;
    private String teacherName;
}
