package flyfish.pojo.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParentQueryDTO {
    private String classNumber;
    private String name;
    private String password;
    private String subject;
}
