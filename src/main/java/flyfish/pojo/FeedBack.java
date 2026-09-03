package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedBack {
    private Integer id;
    private String grade;
    private String className;
    private String teacherName;
    private String subject;
    private String timeZone;
    private LocalDate checkDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String censor;
}
