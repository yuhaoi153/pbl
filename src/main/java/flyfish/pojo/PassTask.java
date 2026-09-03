package flyfish.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassTask {
    private Integer id;
    private String subject;
    private String content;
    private String name;
    private String classNumber;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkdate;
    private LocalDateTime createTime;
    private String studentNumber;
    private Integer completed;
    private String supplementary;

}
