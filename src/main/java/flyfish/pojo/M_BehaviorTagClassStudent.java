package flyfish.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_BehaviorTagClassStudent {
    private HashMap<String, List<M_ClassStudent>> tagClassStudentMap;
}
