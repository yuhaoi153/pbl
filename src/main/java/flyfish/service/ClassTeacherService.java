package flyfish.service;

import java.util.List;
import java.util.Map;

public interface ClassTeacherService {

    /**
     * 通过年级查询教师信息
     * @param grade1
     * @return
     */
    Map<String,List> getByGrade(String grade1);
}
