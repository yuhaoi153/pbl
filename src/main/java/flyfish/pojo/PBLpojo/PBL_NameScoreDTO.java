package flyfish.pojo.PBLpojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PBL_NameScoreDTO {
    private String username;
    private Integer sumScore;
}
