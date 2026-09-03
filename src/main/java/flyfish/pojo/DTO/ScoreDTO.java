package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreDTO {
    private String classNumber;
    private String scoreitem;
    private String value;
    private String subject;
    private LocalDate checkdate;
    private Integer scorenumber;
    private String school;
    private List<String> valueList;
    private Integer year;

}
