package flyfish.pojo;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class M_Certification {
    private  String teacherName;
    private Integer id;
    private Integer userId;
    private String imageUrl;
    private String display;
    private LocalDateTime createTime;
    private String type;
    private String awardLevel;
    private String content;
    private String regionLevel;
    private String userName;
    private String awardName;
    private String personal;
    private String supplement;
    private String judgeRepeat;
    private Integer repeatId;
    //规范日期格式
    //对日期格式进行统一
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate awardTime;
    private String organization;
    private List<String> imageUrls;
    private String school;
}
