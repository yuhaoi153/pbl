package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_BehaviorDTO {
    private String school;
    private String tag;
    private String type;
    private String label;//个人表扬或者个人批评
}
