package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ReadingThreeSituationDTO {
    private String school;
    private List<String> manageClassNameList;
    private List<String> readingClassNameList;
    private List<String> prepareClassNameList;
    private LocalDate checkDate;
    private String grade;//前端传递的年级标签
    private String label;//前端传递的标签
}
