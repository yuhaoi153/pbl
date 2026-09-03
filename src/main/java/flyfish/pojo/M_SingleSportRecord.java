package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_SingleSportRecord {
    private Integer id;
    private String grade;
    private String className;
    private LocalDate checkDate;
    private String timeZone;
}
