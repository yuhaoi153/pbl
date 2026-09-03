package flyfish.pojo.DTO;

import lombok.Data;
import java.util.List;

@Data
public class AmapRouteRequestDTO {
    private String target;
    private String pageName;
    private List<AmapRoutePointDTO> points;
}