package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_HomeworkDTO {
    private String message;
    private String classNumber;
    private String school;
    private String subject;
    private String content;
    private LocalDate checkdate;
    private String supplementary;
    private String minusScoreByHomework;
    private String failRevisionAddScore;
    private Integer addScoreNumber;
    private Integer minusScoreNumber;
    private String phone;
    private String password;
    private String userName;
    private String completedRevisionAddScore;//合格作业订正是否加分，默认是否
    private Integer revisionAddScore;//合格作业订正加分分值，默认是1分
}
