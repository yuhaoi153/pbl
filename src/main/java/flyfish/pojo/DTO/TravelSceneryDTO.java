package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelSceneryDTO {
    private Integer id;
    private String target;
    private String destination;
    private Integer ranklevel;
    private String detail;
    private String supplementary;
    private String shortname;
    private String showImageUrl;
    private String recommendUrl;
    private Integer recommendRank;
    private String creator;
    private String comment;
    private String pageName;
    private Integer order;
}
