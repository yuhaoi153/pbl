package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class M_DeleteReadingFeedbackDTO {
   private List<Integer> ids;
   private LocalDate checkDate;
   private String type;
    private String content;
    private String timeZone;
    private String label;
    private String school;
}
