package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor

@NoArgsConstructor
public class ChartDTO {
    private Integer charttype;//0:班级；1:年级；2:教师；3:学科
    private  Integer duration;
    private LocalDate checkDate;
    private LocalDate startDate;
    private  LocalDate endDate;
    private Integer limit;
    private String grade;
}
