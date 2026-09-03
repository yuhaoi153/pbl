package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ShowHomeWorkDTO {
    private String className;
    private String subject;
    private String school;
    private List<String> messageContentList;
    private List<String> messageTypeList;
    private String userName;
    private String supplementary;
    private LocalDate checkDate;
    private Integer fileSize;
    private Integer duration;
    private String teacherName;
}
