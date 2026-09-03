package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_MoralExcelDTO {
    private String school;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> labelList;
    private String grade;
    private String exportMode;
}
