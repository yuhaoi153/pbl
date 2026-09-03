package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ExportRequestDTO {
    private String userName;
    private String primaryClassifications;
    private String secondaryClassifications;
    private String exportContent;
    private String school;
    private String awardLevel;
    private String regionLevel;
    private String type;
    private String personal;
    private LocalDate startDate;
    private LocalDate endDate;
}
