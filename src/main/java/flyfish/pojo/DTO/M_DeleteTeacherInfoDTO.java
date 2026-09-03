package flyfish.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_DeleteTeacherInfoDTO {
    private List<Integer> idList;
    private String school;
    private String label;
}
