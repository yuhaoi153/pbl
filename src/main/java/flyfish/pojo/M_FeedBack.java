package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_FeedBack {
    private Integer id;
    private String grade;
    private String className;
    private String timeZone;
    private LocalDate checkDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String school;
}
