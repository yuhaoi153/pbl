package flyfish.pojo.VO;


import flyfish.mapper.StudentInfoMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class M_HomeworkStundentInfoVO {
    private String className;
    private List<String> studentNameList;
    private List<String> userNotInHomeworkList;
    private List<String> homeworkNotInUserList;

}
