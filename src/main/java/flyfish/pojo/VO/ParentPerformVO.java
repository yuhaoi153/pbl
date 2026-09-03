package flyfish.pojo.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParentPerformVO {
    @JsonFormat(pattern = "MM/dd")
    private LocalDate checkdate;
    private String name;
    private String subject;
    private String reason;
    private String situation;
    private Integer id;
    private Integer score;
}
