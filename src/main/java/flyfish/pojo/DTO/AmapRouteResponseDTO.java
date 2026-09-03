package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmapRouteResponseDTO {
    private String mapUrl;
    private String distanceText;
    private String durationText;
    private List<AmapRouteSegmentDTO> segments;
}