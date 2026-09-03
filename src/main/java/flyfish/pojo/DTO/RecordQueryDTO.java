package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordQueryDTO {
    private String subject;
    private LocalDate startdate;
    private LocalDate enddate;
    private String content;
    private String classNumber;
    private Integer completed;
    private Integer level;
    private String name;
}
