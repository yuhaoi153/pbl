package flyfish.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class M_Examination {



        private Long id;

        // Excel中的数据
        private String studentName;

        private BigDecimal score;


        private String studentNo;

        // 前端传递的数据
        private String school;

        private String grade;

        private Integer className;

        private String subject;

        private String testName;

        private String createName;

        // 是否发布，例如：未发布、已发布
        private String hide;

        // 以下属性首次导入时不赋值
        private LocalDateTime createTime;

        private String updateName;

        private LocalDateTime updateTime;

        private String imageUrl;

        private String status;

        private Integer year;

        private String semester;
    }
