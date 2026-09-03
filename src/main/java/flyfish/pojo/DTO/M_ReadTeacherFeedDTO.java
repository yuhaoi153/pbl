package flyfish.pojo.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ReadTeacherFeedDTO {
    private Map<String,String> classTeacherMap;
    private Map<String,String> prepareClassTeacherMap;
    private String school;
    private LocalDate checkDate;
    private List<String> readingTeacherList;
    private List<String> prepareTeacherList;
    private String timeZone;
    private String grade;
}
