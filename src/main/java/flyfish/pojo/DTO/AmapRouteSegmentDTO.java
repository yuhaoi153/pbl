package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmapRouteSegmentDTO {
    private String from;
    private String to;
    private String distanceText;
    private String durationText;
}