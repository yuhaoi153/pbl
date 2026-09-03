package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GroupNameList {
    private Integer id;
    private String classNumber;
    private String groupNumber;
    private String name;
    private String subject;
    private LocalDateTime createTime;
    private Integer addscore;
    private Integer punishscore;
    private Integer sumscore;
}
