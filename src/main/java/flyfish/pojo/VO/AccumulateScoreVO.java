package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccumulateScoreVO {
    private String name;
    private Integer addScore;
    private Integer convertScore;
    private Integer sumScore;
    private Integer punishScore;
    private Integer mathaddscore;
    private Integer mathconvertscore;
    private Integer mathsumscore;
    private Integer mathpunishscore;
    private Integer chineseaddscore;
    private Integer chineseconvertscore;
    private Integer chinesesumscore;
    private Integer chinesepunishscore;
    private Integer englishaddscore;
    private Integer englishconvertscore;
    private Integer englishsumscore;
    private Integer englishpunishscore;
    private String pinyin;

}
