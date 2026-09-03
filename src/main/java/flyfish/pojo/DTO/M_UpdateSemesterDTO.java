package flyfish.pojo.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_UpdateSemesterDTO {
    private Integer id;
    private String school;
    private String dataBaseName;
    private String tableName;
    private String dataBaseType;
}
