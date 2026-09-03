package flyfish.pojo.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageQueryNameVO {
    @JsonFormat(pattern = "MM/dd")
    private LocalDate checkdate;
    private String name;
    private List<String> uncompletecontent;
    private List<String> wellcontent;
    private List<String> badcontent;
    private Integer addscore;
    private Integer convertscore;
    private Integer punishscore;
    private Integer sumscore;


}
