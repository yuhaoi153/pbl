package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordTask {
    private Integer id;
    private String subject;
    private String content;
    private String name;
    private String classNumber;
    private LocalDate checkdate;
    private String studentNumber;
    private String supplementary;
    private Integer completed;
}
