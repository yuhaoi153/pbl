package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class M_HardWareDeviceVO {
    private Integer id;
    private String school;
    private String grade;
    private Integer className;
    private String userName;
    private String deviceType;
    private Integer deviceNum;
    private String deviceName;
    private String purpose;
    private String teacherName;
}
