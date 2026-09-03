package flyfish.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ExamEditRecord {
    private Integer id;
    private String updateName;
    private Integer databaseId;
    private String actionWay;
    private String databaseName;
    private String classOrName;
    private String contentInfo;
    private String school;
    private String supplementary;
    private LocalDateTime createTime;

}
