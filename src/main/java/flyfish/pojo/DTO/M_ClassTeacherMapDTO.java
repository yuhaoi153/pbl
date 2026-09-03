package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_ClassTeacherMapDTO {
    private Map<String,String> newPrepareClassTeacherMap;
    private Map<String,String> deleteClassTeacherMap;
    private String school;
}
