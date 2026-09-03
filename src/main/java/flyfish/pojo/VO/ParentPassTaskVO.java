package flyfish.pojo.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParentPassTaskVO {
    private Integer id;
    @JsonFormat(pattern = "MM/dd")
    private LocalDate checkdate;
    private String content;
    private String subject;
    private String supplementary;
    private String name;
}
