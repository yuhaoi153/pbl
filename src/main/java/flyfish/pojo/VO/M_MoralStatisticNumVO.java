package flyfish.pojo.VO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_MoralStatisticNumVO {
    private Integer classPraiseNum;
    private Integer classCriticizeNum;
    private Integer personalPraiseNum;
    private Integer personalCriticizeNum;
}
