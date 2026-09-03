package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenu {

    private Integer id;
    private Integer roleId;
    private Integer menuId;
    private String name;
}
