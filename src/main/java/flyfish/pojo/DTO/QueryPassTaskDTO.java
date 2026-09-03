package flyfish.pojo.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QueryPassTaskDTO {
    private LocalDate startdate;
    private LocalDate enddate;
    private String classNumber;
    private String content;
    private String subject;
    private String name;
    private Integer completed;
}
