package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccumulateScore {
    private Integer addscore;
    private Integer minusscore;
    private Integer punishscore;
    private String reason;
    private String name;
    private String classNumber;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String subject;
    private String school;
    private String pinyin;
}
