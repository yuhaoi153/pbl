package flyfish.service;

import java.util.List;

public interface StudentInfoService {
    /**
     * 根据班级查询姓名
     * @param classNumber
     * @return
     */
    List<String> getNameByClass(String classNumber);
}
