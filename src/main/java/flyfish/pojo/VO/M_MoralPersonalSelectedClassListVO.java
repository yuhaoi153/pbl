package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_MoralPersonalSelectedClassListVO {
    private HashMap<String, List<String>> personalPraiseSelectedClassListMap;
    private HashMap<String, List<String>> personalCriticizeSelectedClassListMap;
}
