package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_HardwareDevice {
    private Integer id;
    private String school;
    private String grade;
    private Integer className;
    private String deviceType;
    private Integer deviceNum;
    private String userName;
    private String purpose;
    private String superVisor;
}
