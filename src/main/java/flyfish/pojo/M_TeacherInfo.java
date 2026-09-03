package flyfish.pojo;

import lombok.Data;

@Data
public class M_TeacherInfo {
    private Integer id;
    private String teacherName;
    private String subject;
    private String className;
    private String school;
    private String grade;
}