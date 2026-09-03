package flyfish.pojo.PBLpojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PBL_VoteResult {
    private String voteA;
    private String voteB;
    private String voteC;
    private String voteD;
    private String voteE;
    private String voteF;
    private List<String> votersChooseA;
    private List<String> votersChooseB;
    private List<String> votersChooseC;
    private List<String> votersChooseD;
    private List<String> votersChooseE;
    private List<String> votersChooseF;
}
