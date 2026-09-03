package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_DefaultConfig {
    private Integer id;
    private String textConfig;
    private Integer intConfig;
    private String infoName;
    private String school;
    private String userName;
}
