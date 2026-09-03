package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassTeacher {
    private Integer id;
    private String className;
    private String mathTeacher;
    private String chineseTeacher;
    private String englishTeacher;
    private String grade;
}
