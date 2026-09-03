package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelImageRecordDTO {
    private Integer id;
    private String imageUrl;
    private String name;
    private String type;
    private String target;
    private String pageName;

}
