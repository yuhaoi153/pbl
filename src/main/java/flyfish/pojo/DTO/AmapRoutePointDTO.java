package flyfish.pojo.DTO;

import lombok.Data;

@Data
public class AmapRoutePointDTO {
    private String name;
    private Double lon;
    private Double lat;
    private String alt;
    private Integer order;
}