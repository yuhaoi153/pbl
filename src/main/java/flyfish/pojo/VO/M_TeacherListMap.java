package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_TeacherListMap {
    private Integer id;
    private Map<String, List<String>> teacherDataMap;
    private Map<String,List<String>> prepareTeacherDataMap;
}
