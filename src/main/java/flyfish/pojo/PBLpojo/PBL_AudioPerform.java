package flyfish.pojo.PBLpojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PBL_AudioPerform {
    private Integer score;//大模型给的分数
    private String comment;//大模型给的评语
}
