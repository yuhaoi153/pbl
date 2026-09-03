package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Menu {
    private Integer id;
    private String name;
    private Integer pid;
    private String path;
    private String component;
    private String icon;
    private Integer userId;
}
