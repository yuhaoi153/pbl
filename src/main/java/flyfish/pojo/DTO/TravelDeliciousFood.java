package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelDeliciousFood {
    private Integer id;
    private String target;
    private String name;
    private String destinationUrl;
    private Integer ranklevel;
    private String detail;
    private String supplementary;
    private String recommendUrl;
    private Integer recommendRank;
    private String comment;
    private String pageName;
}
