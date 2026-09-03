package flyfish.pojo.DTO;

import flyfish.pojo.M_BehaviorTagClassStudent;
import flyfish.pojo.M_ClassStudent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_MoralStudentBehaviorListDTO {
    private String tag;
    private String label;
    private String grade;
    private String className;
    private HashMap<String, List<M_ClassStudent>> personalPraiseBehaviorMap;
    private HashMap<String, List<M_ClassStudent>> personalCriticizeBehaviorMap;
    private String school;
    private LocalDate checkDate;


}
