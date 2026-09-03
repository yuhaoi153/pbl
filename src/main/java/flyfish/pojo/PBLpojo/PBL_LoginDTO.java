package flyfish.pojo.PBLpojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PBL_LoginDTO {
    private String school;
    private String password;
    private String username;
    private String menuName;
}
