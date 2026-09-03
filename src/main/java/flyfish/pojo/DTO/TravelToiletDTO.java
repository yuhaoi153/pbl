package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelToiletDTO {
    private String id;
    private String name;
    private String address;
    private String location;
    private String distance;
    private String mapUrl;
}