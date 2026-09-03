package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutoDataVO {
    private List<String> teachernamelist;
    private List<String> classnamelist;
}
