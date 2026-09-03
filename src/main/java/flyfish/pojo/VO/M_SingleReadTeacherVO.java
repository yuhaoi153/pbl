package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_SingleReadTeacherVO {
    private Map<String,String> readingClassTeacherMap;
    private Map<String,String> prepareClassTeacherMap;
}
