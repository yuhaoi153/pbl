package flyfish.pojo.VO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ExaminationExcelVO {
    /**
     * 成功导入数量，包括缺考学生
     */
    private Integer importedCount;

    /**
     * 导入失败的学生名单
     */
    private List<String> failStudentNameList;
}
