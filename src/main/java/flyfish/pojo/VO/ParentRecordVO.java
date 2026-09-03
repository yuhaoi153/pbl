package flyfish.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ParentRecordVO {
    private LocalDate checkdate;
    private String uncompletecontent;
    private String wellcontent;
    private String badcontent;
}
