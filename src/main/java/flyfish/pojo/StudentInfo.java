package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentInfo {
    private Integer id;
    private String name;
    private String studentNumber;
    private String classNumber;
    private String pinyin;
    private String school;
    private Integer year;
}
