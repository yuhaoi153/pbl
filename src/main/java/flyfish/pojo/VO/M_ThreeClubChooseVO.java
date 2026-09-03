package flyfish.pojo.VO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ThreeClubChooseVO {
    private Integer id;
    private String name;
    private String teacher;
    private String category;
    private String resultStatus;
    private String resultText;
}
