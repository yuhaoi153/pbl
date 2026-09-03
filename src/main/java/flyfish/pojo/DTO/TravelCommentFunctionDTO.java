package flyfish.pojo.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelCommentFunctionDTO {
    private Integer id;
    private Integer replyId;//关联id
    private String target;
    private String name;//标题名称
    private String userName;
    private Integer ranklevel;
    private String detail;
    private String supplementary;
    private String pageName;
    private LocalDateTime createTime;
}
