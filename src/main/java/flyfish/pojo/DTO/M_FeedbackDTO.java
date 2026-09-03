package flyfish.pojo.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_FeedbackDTO {
    private String school;
    private LocalDate checkDate;
    private String type;
    private String content;
    private String timeZone;
    private String label;

}
