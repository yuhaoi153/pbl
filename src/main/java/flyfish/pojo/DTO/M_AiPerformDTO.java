package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_AiPerformDTO {
    private String message;
    private String school;
    private String classNumber;
    private String subject;
}
