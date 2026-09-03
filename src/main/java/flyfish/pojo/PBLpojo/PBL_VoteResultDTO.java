package flyfish.pojo.PBLpojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PBL_VoteResultDTO {
    private List<Integer> questionIdList;
    private LocalDate checkDate;
}
