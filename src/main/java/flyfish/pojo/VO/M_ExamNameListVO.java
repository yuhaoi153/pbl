package flyfish.pojo.VO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ExamNameListVO {
    List<String> preSetExamNameList;//预设的考试名称
    List<String> historyExamNameList;//已经考过的考试名称
}
