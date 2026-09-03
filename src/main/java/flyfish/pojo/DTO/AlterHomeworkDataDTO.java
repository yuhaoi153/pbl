package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlterHomeworkDataDTO {
    private String school;
    private String classNumber;
    private String subject;
    private String content;
    private LocalDate checkdate;
    private List<String> revisionListGeneral;
    private List<String> uncompletelist;
    private String supplementary;
    private String alterType;//全部变为未提交，全部变为已提交
}
