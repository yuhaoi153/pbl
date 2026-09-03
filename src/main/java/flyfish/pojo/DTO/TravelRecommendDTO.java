package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelRecommendDTO {
    private Integer id;
    private String target;
    private String name;
    private String type;
    private String recommendOriginData;

    private String recommendUrl;
    private Integer recommendRank;
    private String comment;
    private String pageName;
}
