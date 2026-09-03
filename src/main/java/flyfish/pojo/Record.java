package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Record {
    private Integer id;
    private Integer startpage;
    private Integer endpage;
    private String subject;
    private String content;
    private String name;
    private String classNumber;
    private String type;
    private Integer level;
    private Boolean revision;
    private LocalDate checkdate;
    private LocalDateTime createTime;
    private String studentNumber;
    private Integer completed;
    private String supplementary;
    private String school;

}
