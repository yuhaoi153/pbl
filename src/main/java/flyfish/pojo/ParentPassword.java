package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParentPassword {
    private Integer id;

    private String classNumber;
    private String name;
    private String password;
    private Integer queryTime;
}
