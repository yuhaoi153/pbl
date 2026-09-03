package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ReadGradeFeedDTO {
    private String timeZone;
    private String school;
    private LocalDate checkDate;
    private String grade;
    private List<String> grade1ClassList;
    private List<String> grade2ClassList;
    private List<String> grade3ClassList;
    private List<String> grade4ClassList;
    private List<String> grade5ClassList;
    private List<String> grade6ClassList;
    private List<String> grade7ClassList;
    private List<String> grade8ClassList;
    private List<String> grade9ClassList;
}
