package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditStudentForm {
    private Integer id;
    private String studentName;
    private String className;
    private String password;
    private Integer queryCount;
}
