package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIAudioDTO {
    private String classNumber;
    private String subject;
    private String message;
    private String school;
}
