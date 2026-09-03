package flyfish.pojo.VO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_UpdateTableVO {
    private  String dataBaseName;//数据库名
    private String tableName;//表名
    private Integer updateCount;//更新的次数
    private String updateStatus;//更新状态，成功或失败
    private String dataBaseType;//
}
