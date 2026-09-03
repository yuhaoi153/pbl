package flyfish.pojo.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RecordVO {
    private Integer id;
    private String subject;
    private String content;
    private String name;
    private String classNumber;
    private Integer level;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkdate;
    private String studentNumber;
    private Integer completed;
}
