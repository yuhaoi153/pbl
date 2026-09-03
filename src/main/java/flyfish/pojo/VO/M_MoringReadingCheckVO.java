package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_MoringReadingCheckVO {
    private Integer id;
    private String name;
    private String weekday;
    private String feedbackPeople;
    private String checkZone;
    private String authority;

}
